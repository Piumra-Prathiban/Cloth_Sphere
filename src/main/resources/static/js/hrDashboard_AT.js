// Attendance Tracking Functions
let currentAttendanceData = [];
let employeeList = [];

// Load employee list for auto-suggest
async function loadEmployeeList() {
    try {
        const response = await fetchWithAuth('/api/employees');
        if (response.ok) {
            const employees = await response.json();
            employeeList = employees.map(emp => ({
                id: emp.id,
                name: emp.fullName,
                department: emp.department ? emp.department.departmentName : 'N/A'
            }));
            console.log('Employee list loaded:', employeeList.length);
        }
    } catch (error) {
        console.error('Error loading employee list:', error);
    }
}

// Auto-suggest for employee search
function setupEmployeeAutoSuggest() {
    const idInput = document.getElementById('employeeSearch');
    const nameInput = document.getElementById('employeeNameSearch');

    // Employee ID auto-suggest
    idInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        if (searchTerm.length < 2) {
            clearSuggestions();
            return;
        }

        const suggestions = employeeList.filter(emp =>
            emp.id.toLowerCase().includes(searchTerm) ||
            emp.name.toLowerCase().includes(searchTerm)
        );

        showSuggestions(suggestions, this, 'id');
    });

    // Employee Name auto-suggest
    nameInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        if (searchTerm.length < 2) {
            clearSuggestions();
            return;
        }

        const suggestions = employeeList.filter(emp =>
            emp.name.toLowerCase().includes(searchTerm) ||
            emp.id.toLowerCase().includes(searchTerm)
        );

        showSuggestions(suggestions, this, 'name');
    });

    // Clear suggestions when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.matches('#employeeSearch, #employeeNameSearch, .suggestion-item')) {
            clearSuggestions();
        }
    });
}

function showSuggestions(suggestions, inputElement, type) {
    clearSuggestions();

    if (suggestions.length === 0) return;

    const dropdown = document.createElement('div');
    dropdown.className = 'suggestions-dropdown';

    suggestions.slice(0, 10).forEach(emp => {
        const item = document.createElement('div');
        item.className = 'suggestion-item';
        item.innerHTML = `
            <div>${emp.name} <span class="employee-id">(${emp.id})</span></div>
            <small style="color: #6c757d;">${emp.department}</small>
        `;
        item.addEventListener('click', function() {
            if (type === 'id') {
                document.getElementById('employeeSearch').value = emp.id;
                document.getElementById('employeeNameSearch').value = emp.name;
            } else {
                document.getElementById('employeeNameSearch').value = emp.name;
                document.getElementById('employeeSearch').value = emp.id;
            }
            clearSuggestions();
        });
        dropdown.appendChild(item);
    });

    inputElement.parentNode.appendChild(dropdown);
}

function clearSuggestions() {
    const existingDropdown = document.querySelector('.suggestions-dropdown');
    if (existingDropdown) {
        existingDropdown.remove();
    }
}

// Enhanced search attendance function
// Enhanced search attendance function with better error handling
async function searchAttendance() {
    try {
        const employeeId = document.getElementById('employeeSearch')?.value.trim();
        const fromDate = document.getElementById('fromDate')?.value;
        const toDate = document.getElementById('toDate')?.value;

        // Check if elements exist
        if (!fromDate || !toDate) {
            console.error('Required date elements not found');
            showAlert('Please select both From and To dates', 'error');
            return;
        }

        // Validate inputs
        if (!fromDate || !toDate) {
            showAlert('Please select both From and To dates', 'error');
            return;
        }

        // Show loading state safely
        const searchBtn = document.querySelector('.btn-primary');
        if (searchBtn) {
            const originalText = searchBtn.innerHTML;
            searchBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Searching...';
            searchBtn.disabled = true;
        }

        // Build query parameters
        const params = new URLSearchParams({
            fromDate: fromDate,
            toDate: toDate
        });

        // Add employee ID filter if provided
        if (employeeId) params.append('employeeId', employeeId);

        const response = await fetchWithAuth(`/attendance/hr/all-attendance?${params}`);

        // Restore button state safely
        if (searchBtn) {
            searchBtn.innerHTML = originalText;
            searchBtn.disabled = false;
        }

        if (response.ok) {
            const result = await response.json();

            if (result.success) {
                currentAttendanceData = result.attendanceRecords || [];

                if (currentAttendanceData.length > 0) {
                    displayAllEmployeesAttendanceSummary(result.summary);
                    displayAttendanceDetails(currentAttendanceData);
                    // Safely show/hide elements
                    const summaryEl = document.getElementById('attendanceSummary');
                    const noDataEl = document.getElementById('noAttendanceData');
                    if (summaryEl) summaryEl.style.display = 'block';
                    if (noDataEl) noDataEl.style.display = 'none';
                    showAlert('Attendance records loaded successfully', 'success');
                } else {
                    const summaryEl = document.getElementById('attendanceSummary');
                    const noDataEl = document.getElementById('noAttendanceData');
                    if (summaryEl) summaryEl.style.display = 'none';
                    if (noDataEl) noDataEl.style.display = 'block';
                    showAlert('No attendance records found for the selected criteria', 'info');
                }
            } else {
                showAlert(result.message || 'Failed to fetch attendance data', 'error');
            }
        } else {
            const errorText = await response.text();
            let errorMessage = 'Failed to fetch attendance data';
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorMessage;
            } catch (e) {
                errorMessage = errorText || errorMessage;
            }
            showAlert(errorMessage, 'error');
        }
    } catch (error) {
        console.error('Error searching attendance:', error);
        showAlert('Error searching attendance: ' + error.message, 'error');

        // Restore button state in case of error
        const searchBtn = document.querySelector('.btn-primary');
        if (searchBtn) {
            searchBtn.innerHTML = '<i class="fas fa-search"></i> Search';
            searchBtn.disabled = false;
        }
    }
}

// Safe element access for summary display
function displayAllEmployeesAttendanceSummary(summary) {
    const elements = {
        summaryEmpId: document.getElementById('summaryEmpId'),
        summaryEmpName: document.getElementById('summaryEmpName'),
        summaryTotalDays: document.getElementById('summaryTotalDays'),
        summaryPresentDays: document.getElementById('summaryPresentDays'),
        summaryAbsentDays: document.getElementById('summaryAbsentDays'),
        summaryWorkHours: document.getElementById('summaryWorkHours'),
        summaryAttendanceRate: document.getElementById('summaryAttendanceRate')
    };

    // Safely update elements if they exist
    if (elements.summaryEmpId) {
        elements.summaryEmpId.textContent =
            summary.searchCriteria && summary.searchCriteria.employeeId ?
                summary.searchCriteria.employeeId : 'ALL EMPLOYEES';
    }

    if (elements.summaryEmpName) {
        elements.summaryEmpName.textContent = `Total Employees: ${summary.totalEmployees || 0}`;
    }

    if (elements.summaryTotalDays) elements.summaryTotalDays.textContent = summary.totalRecords || 0;
    if (elements.summaryPresentDays) elements.summaryPresentDays.textContent = summary.totalPresentDays || 0;
    if (elements.summaryAbsentDays) elements.summaryAbsentDays.textContent = summary.totalAbsentDays || 0;
    if (elements.summaryWorkHours) elements.summaryWorkHours.textContent = summary.totalWorkHours || 0;

    const attendanceRate = summary.totalRecords > 0 ?
        Math.round((summary.totalPresentDays / summary.totalRecords) * 100) : 0;
    if (elements.summaryAttendanceRate) elements.summaryAttendanceRate.textContent = attendanceRate + '%';
}

// Display attendance details table
// Update the displayAttendanceDetails function to include Employee ID
function displayAttendanceDetails(records) {
    const tbody = document.querySelector('#attendanceTable tbody');
    tbody.innerHTML = '';

    records.forEach(record => {
        const row = document.createElement('tr');

        // Format work hours to 2 decimal places
        const workHours = record.workHours ?
            Math.round(record.workHours * 100) / 100 : '-';

        // Get day name
        const date = new Date(record.attendanceDate);
        const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });

        row.innerHTML = `
            <td>${record.employeeId}</td>
            <td>${record.attendanceDate}</td>
            <td>${dayName}</td>
            <td>${record.checkInTime || '-'}</td>
            <td>${record.checkOutTime || '-'}</td>
            <td>${workHours}</td>
            <td><span class="status-badge status-${record.status.toLowerCase()}">${record.status}</span></td>
            <td>${record.notes || '-'}</td>
        `;

        tbody.appendChild(row);
    });
}

// Reset attendance filters
function resetAttendanceFilters() {
    document.getElementById('employeeSearch').value = '';
    document.getElementById('fromDate').value = '';
    document.getElementById('toDate').value = '';
    document.getElementById('attendanceSummary').style.display = 'none';
    document.getElementById('noAttendanceData').style.display = 'none';
    document.querySelector('#attendanceTable tbody').innerHTML = '';
    currentAttendanceData = [];
    clearSuggestions();
}

// Utility function for authenticated fetch
async function fetchWithAuth(url, options = {}) {
    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    const mergedOptions = { ...defaultOptions, ...options };
    return await fetch(url, mergedOptions);
}

// Alert function
function showAlert(message, type = 'info') {
    // Implement your alert system here
    console.log(`${type.toUpperCase()}: ${message}`);
    alert(message); // Simple fallback
}

// Set default dates (current month)
function setDefaultDates() {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);

    document.getElementById('fromDate').value = formatDate(firstDay);
    document.getElementById('toDate').value = formatDate(today);
}

function formatDate(date) {
    return date.toISOString().split('T')[0];
}

// Initialize attendance section when page loads
document.addEventListener('DOMContentLoaded', function() {
    setDefaultDates();
    loadEmployeeList().then(() => {
        setupEmployeeAutoSuggest();
    });
});