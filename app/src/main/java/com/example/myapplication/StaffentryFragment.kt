package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.example.myapplication.databinding.FragmentStaffEntryBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ── Data Models ──

data class Staff(
    val id: String,
    val name: String,
    val phone: String,
    val role: String,
    val department: String,
    val shiftStart: String,
    val shiftEnd: String,
    val isActive: Boolean = true
)

enum class AttendanceType { LOGIN, LOGOUT }

data class AttendanceRecord(
    val staffId: String,
    val type: AttendanceType,
    val timestamp: Long = System.currentTimeMillis()
)

// ── ViewModel ──

class StaffEntryViewModel : ViewModel() {

    private val staffRepository = MockStaffRepository()

    private val _staffResult = MutableLiveData<Staff?>()
    val staffResult: LiveData<Staff?> = _staffResult

    private val _attendanceState = MutableLiveData<AttendanceState>()
    val attendanceState: LiveData<AttendanceState> = _attendanceState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun searchStaff(phone: String) {
        if (phone.length != 10) {
            _staffResult.value = null
            _attendanceState.value = AttendanceState.Error("Enter a valid 10-digit number")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            delay(600)
            val staff = staffRepository.findByPhone(phone)

            _staffResult.value = staff
            _attendanceState.value = if (staff == null)
                AttendanceState.Error("No staff found")
            else
                AttendanceState.Idle

            _isLoading.value = false
        }
    }

    fun markAttendance(staffId: String, type: AttendanceType) {
        _isLoading.value = true

        viewModelScope.launch {
            delay(400)

            val record = AttendanceRecord(staffId, type)
            staffRepository.saveAttendance(record)

            val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(record.timestamp))

            _attendanceState.value = when (type) {
                AttendanceType.LOGIN -> AttendanceState.Success("Login at $time", type)
                AttendanceType.LOGOUT -> AttendanceState.Success("Logout at $time", type)
            }

            _isLoading.value = false
        }
    }

    fun reset() {
        _staffResult.value = null
        _attendanceState.value = AttendanceState.Idle
    }

    sealed class AttendanceState {
        object Idle : AttendanceState()
        data class Success(val message: String, val type: AttendanceType) : AttendanceState()
        data class Error(val message: String) : AttendanceState()
    }
}

// ── Mock Repository ──

class MockStaffRepository {
    private val staffList = listOf(
        Staff("S001", "Ravi Kumar", "9876543210", "Manager", "Operations", "09:00 AM", "06:00 PM"),
        Staff("S002", "Priya Sharma", "9123456789", "Supervisor", "Security", "02:00 PM", "11:00 PM"),
        Staff("S003", "Arjun Nair", "9988776655", "Gate Officer", "Front Desk", "06:00 AM", "02:00 PM"),
    )

    private val attendanceLogs = mutableListOf<AttendanceRecord>()

    fun findByPhone(phone: String): Staff? {
        return staffList.find { it.phone == phone }
    }

    fun saveAttendance(record: AttendanceRecord) {
        attendanceLogs.add(record)
    }
}

// ── Fragment ──

class StaffEntryFragment : Fragment() {

    private var _binding: FragmentStaffEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupInputListeners()
        setupButtons()
        observeVM()
    }

    private fun setupInputListeners() {
        binding.etStaffPhone.doAfterTextChanged {
            binding.btnSearchStaff.isEnabled = it?.length == 10
            if (it?.length == 10) hideStatus()
        }

        binding.etStaffPhone.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch()
                true
            } else false
        }
    }

    private fun setupButtons() {

        binding.btnSearchStaff.setOnClickListener { triggerSearch() }

        binding.btnMarkLogin.setOnClickListener {
            viewModel.staffResult.value?.let {
                viewModel.markAttendance(it.id, AttendanceType.LOGIN)
            }
        }

        binding.btnMarkLogout.setOnClickListener {
            viewModel.staffResult.value?.let {
                viewModel.markAttendance(it.id, AttendanceType.LOGOUT)
            }
        }

        // ✅ FIXED BACK BUTTON
        binding.btnBack.setOnClickListener {
            viewModel.reset()
            binding.etStaffPhone.text?.clear()
            dismissKeyboard()
            requireActivity().finish()
        }
    }

    private fun triggerSearch() {
        val phone = binding.etStaffPhone.text.toString().trim()
        dismissKeyboard()
        viewModel.searchStaff(phone)
    }

    private fun observeVM() {

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.btnSearchStaff.isEnabled = !it
        }

        viewModel.staffResult.observe(viewLifecycleOwner) {
            if (it != null) showStaff(it) else hideStaff()
        }

        viewModel.attendanceState.observe(viewLifecycleOwner) {
            when (it) {
                is StaffEntryViewModel.AttendanceState.Idle -> hideStatus()
                is StaffEntryViewModel.AttendanceState.Success -> showStatus(it.message, false)
                is StaffEntryViewModel.AttendanceState.Error -> showStatus(it.message, true)
            }
        }
    }

    private fun showStaff(staff: Staff) {
        binding.cardStaffResult.isVisible = true
        binding.layoutActions.isVisible = true

        binding.tvStaffName.text = staff.name
        binding.tvStaffRole.text = staff.role
        binding.tvDepartment.text = staff.department
        binding.tvShift.text = "${staff.shiftStart} - ${staff.shiftEnd}"
    }

    private fun hideStaff() {
        binding.cardStaffResult.isVisible = false
        binding.layoutActions.isVisible = false
    }

    private fun showStatus(msg: String, error: Boolean) {
        binding.layoutStatus.isVisible = true
        binding.tvStatus.text = msg
    }

    private fun hideStatus() {
        binding.layoutStatus.isVisible = false
    }

    private fun dismissKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}