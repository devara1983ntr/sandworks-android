package com.roshan.sandworks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.roshan.sandworks.data.repository.SandWorksRepository
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class SandWorksViewModel(
    private val repository: SandWorksRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _selectedDate = MutableStateFlow(LocalDate.now().format(dateFormatter))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userRole = MutableStateFlow(UserRole.OWNER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _selectedPersonId = MutableStateFlow<String?>(null)
    val selectedPersonId: StateFlow<String?> = _selectedPersonId.asStateFlow()

    fun setUserRole(role: UserRole, personId: String? = null) {
        _userRole.value = role
        _selectedPersonId.value = personId
    }

    fun goToPreviousDay() {
        val current = try { LocalDate.parse(_selectedDate.value) } catch (e: Exception) { LocalDate.now() }
        setSelectedDate(current.minusDays(1).format(dateFormatter))
    }

    fun goToNextDay() {
        val current = try { LocalDate.parse(_selectedDate.value) } catch (e: Exception) { LocalDate.now() }
        setSelectedDate(current.plusDays(1).format(dateFormatter))
    }

    fun goToToday() {
        setSelectedDate(LocalDate.now().format(dateFormatter))
    }

    val allPeople: StateFlow<List<Person>> = repository.allPeople
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePeople: StateFlow<List<Person>> = repository.activePeople
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTractors: StateFlow<List<Tractor>> = repository.allTractors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTractors: StateFlow<List<Tractor>> = repository.activeTractors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tripsForSelectedDate: StateFlow<List<TripWithDetails>> = _selectedDate
        .flatMapLatest { date -> repository.getTripsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceForSelectedDate: StateFlow<List<Attendance>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailySummary = MutableStateFlow(
        DailySummary(
            workDate = _selectedDate.value,
            status = ClosureStatus.NO_WORK,
            totalTrips = 0,
            activeTrips = 0,
            voidedTrips = 0,
            grossPaise = 0L,
            distributedPaise = 0L,
            remainingPaise = 0L,
            tractorTripCounts = emptyMap(),
            lastCalculated = 0L,
            lastChanged = 0L
        )
    )
    val dailySummary: StateFlow<DailySummary> = _dailySummary.asStateFlow()

    private val _weeklyLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val weeklyLeaderboard: StateFlow<List<LeaderboardEntry>> = _weeklyLeaderboard.asStateFlow()

    private val _monthlyLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val monthlyLeaderboard: StateFlow<List<LeaderboardEntry>> = _monthlyLeaderboard.asStateFlow()

    private val _participationMatrix = MutableStateFlow<List<ParticipationMatrixRow>>(emptyList())
    val participationMatrix: StateFlow<List<ParticipationMatrixRow>> = _participationMatrix.asStateFlow()

    val activeEmergencyAlert = repository.activeEmergencyAlert
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun broadcastEmergencyAlert(title: String, message: String, severity: String = "URGENT", targetRole: String = "ALL") {
        viewModelScope.launch {
            repository.sendEmergencyAlert(title, message, severity, targetRole)
        }
    }

    fun acknowledgeEmergencyAlert(alertId: String) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }


    private val _exceptions = MutableStateFlow<List<ExceptionItem>>(emptyList())
    val exceptions: StateFlow<List<ExceptionItem>> = _exceptions.asStateFlow()

    val auditEvents: StateFlow<List<AuditEvent>> = repository.allAuditEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshDateData(_selectedDate.value)
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        refreshDateData(date)
    }

    fun refreshDateData(date: String) {
        viewModelScope.launch {
            _dailySummary.value = repository.getDailySummary(date)
            _participationMatrix.value = repository.getParticipationMatrix(date)
            _exceptions.value = repository.getExceptions(date)

            val parsedDate = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }
            _weeklyLeaderboard.value = repository.getWeeklyLeaderboard(parsedDate)
            _monthlyLeaderboard.value = repository.getMonthlyLeaderboard(YearMonth.from(parsedDate))
        }
    }

    fun createTrip(
        tractorId: String,
        driverId: String?,
        ratePaise: Long,
        participants: List<MoneyEngine.ParticipantInput>,
        adjustments: Map<String, Long> = emptyMap(),
        onSuccess: (tripId: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val tripId = repository.createTripAtomic(
                workDate = _selectedDate.value,
                tractorId = tractorId,
                driverId = driverId,
                ratePaise = ratePaise,
                participants = participants,
                adjustments = adjustments
            )
            refreshDateData(_selectedDate.value)
            onSuccess(tripId)
        }
    }

    fun voidTrip(tripId: String, reason: String) {
        viewModelScope.launch {
            repository.voidTrip(tripId, reason)
            refreshDateData(_selectedDate.value)
        }
    }

    fun editTrip(
        tripId: String,
        tractorId: String,
        driverId: String?,
        ratePaise: Long,
        participants: List<MoneyEngine.ParticipantInput>,
        adjustments: Map<String, Long> = emptyMap()
    ) {
        viewModelScope.launch {
            repository.editTrip(tripId, tractorId, driverId, ratePaise, participants, adjustments)
            refreshDateData(_selectedDate.value)
        }
    }

    fun closeDay() {
        viewModelScope.launch {
            repository.closeDay(_selectedDate.value)
            refreshDateData(_selectedDate.value)
        }
    }

    fun reopenDay() {
        viewModelScope.launch {
            repository.reopenDay(_selectedDate.value)
            refreshDateData(_selectedDate.value)
        }
    }

    fun setAttendance(personId: String, status: AttendanceStatus, notes: String? = null) {
        viewModelScope.launch {
            repository.setAttendance(personId, _selectedDate.value, status, notes)
            refreshDateData(_selectedDate.value)
        }
    }

    fun markAllPresent(peopleIds: List<String>) {
        viewModelScope.launch {
            val date = _selectedDate.value
            for (pid in peopleIds) {
                repository.setAttendance(pid, date, AttendanceStatus.PRESENT, "Batch Marked Present")
            }
            refreshDateData(date)
        }
    }

    fun autoMarkLoadedWorkersPresent(loadedIds: Set<String>) {
        viewModelScope.launch {
            val date = _selectedDate.value
            for (pid in loadedIds) {
                repository.setAttendance(pid, date, AttendanceStatus.PRESENT, "Auto Marked - Loaded Trips Today")
            }
            refreshDateData(date)
        }
    }

    fun addPerson(name: String, type: PersonType) {
        viewModelScope.launch {
            repository.addPerson(name, type)
            refreshDateData(_selectedDate.value)
        }
    }

    fun togglePersonActive(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person.copy(active = !person.active))
            refreshDateData(_selectedDate.value)
        }
    }

    fun addTractor(name: String, brand: String, model: String, spec: String, reg: String) {
        viewModelScope.launch {
            repository.addTractor(name, brand, model, spec, reg)
            refreshDateData(_selectedDate.value)
        }
    }

    fun seedDefaultFleet() {
        viewModelScope.launch {
            repository.addTractor("Tractor 1", "Mahindra", "575 DI", "Standard Sand Carting Trailer", "TR-01")
            repository.addTractor("Tractor 2", "Swaraj", "744 FE", "Heavy Duty Steel Trailer", "TR-02")
            repository.addTractor("Tractor 3", "John Deere", "5050 D", "Reinforced Sand Bed", "TR-03")
            refreshDateData(_selectedDate.value)
        }
    }

    suspend fun getPersonSummary(personId: String): PersonSummary {
        return repository.getPersonSummary(personId)
    }

    fun generateShareSummaryText(level: String = "QUICK"): String {
        val summary = _dailySummary.value
        val trips = tripsForSelectedDate.value
        val matrix = _participationMatrix.value

        val sb = StringBuilder()
        sb.append("SAND WORKS — Daily Operational Summary\n")
        sb.append("Date: ${summary.workDate}\n")
        sb.append("Total Trips: ${summary.totalTrips} (Active: ${summary.activeTrips}, Voided: ${summary.voidedTrips})\n")
        sb.append("Gross Pool: ${MoneyEngine.formatPaise(summary.grossPaise)}\n")
        sb.append("Distributed: ${MoneyEngine.formatPaise(summary.distributedPaise)}\n")
        sb.append("Remaining: ${MoneyEngine.formatPaise(summary.remainingPaise)}\n\n")

        if (level == "QUICK") {
            sb.append("Labourers & Drivers Participation:\n")
            for (row in matrix) {
                sb.append("• ${row.person.name}: ${row.totalTrips} trips\n")
            }
        } else {
            sb.append("Trip Breakdown:\n")
            for (td in trips.filter { it.trip.status == TripStatus.ACTIVE }) {
                sb.append("Trip #${td.trip.tripNumber} (${td.tractor?.name ?: "Tractor"}) — ")
                val pNames = td.participants.joinToString { "${it.person.name} (${MoneyEngine.formatPaise(it.participant.finalSharePaise)})" }
                sb.append("$pNames | Rem: ${MoneyEngine.formatPaise(td.calculation?.remainingPaise ?: 0L)}\n")
            }
        }
        sb.append("\nRecorded via SAND WORKS (Local Device Storage)")
        return sb.toString()
    }

    fun editTrip(
        tripId: String,
        tractorId: String,
        driverId: String?,
        participants: List<MoneyEngine.ParticipantInput>,
        adjustments: Map<String, Long> = emptyMap(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.editTrip(
                tripId = tripId,
                tractorId = tractorId,
                driverId = driverId,
                participants = participants,
                adjustments = adjustments
            )
            refreshDateData(_selectedDate.value)
            onSuccess()
        }
    }

    suspend fun createBackupJson(): String {
        return repository.createBackupJson()
    }

    fun restoreBackupJson(jsonString: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreBackupJson(jsonString)
            refreshDateData(_selectedDate.value)
            onResult(result)
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            refreshDateData(_selectedDate.value)
            onComplete()
        }
    }

    fun selectPerson(personId: String?) {
        _selectedPersonId.value = personId
    }

    suspend fun runDataHealthCheck(): DataHealthReport {
        return repository.runDataHealthCheck()
    }

    fun generateCsv(): String {
        val trips = tripsForSelectedDate.value
        val sb = StringBuilder()
        sb.append("Date,TripNumber,Tractor,Rate,Status,ParticipantCount,Distributed,Remaining,Participants\n")
        for (td in trips) {
            val participantsStr = td.participants.joinToString(";") { "${it.person.name}:${it.participant.finalSharePaise / 100}" }
            sb.append("${td.trip.workDate},${td.trip.tripNumber},\"${td.tractor?.name ?: ""}\",${td.trip.ratePaise / 100},${td.trip.status},${td.calculation?.participantCount ?: 0},${(td.calculation?.distributedPaise ?: 0L) / 100},${(td.calculation?.remainingPaise ?: 0L) / 100},\"$participantsStr\"\n")
        }
        return sb.toString()
    }
}

class SandWorksViewModelFactory(
    private val repository: SandWorksRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SandWorksViewModel::class.java)) {
            return SandWorksViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
