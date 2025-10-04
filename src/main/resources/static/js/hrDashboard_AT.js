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
// Fixed auto-suggest function with proper closing
function setupEmployeeAutoSuggest() {
    const idInput = document.getElementById('employeeSearch');

    // Ensure the input is in a suggestions-container
    let container = idInput.parentElement;
    if (!container.classList.contains('suggestions-container')) {
        const newContainer = document.createElement('div');
        newContainer.className = 'suggestions-container';
        idInput.parentNode.insertBefore(newContainer, idInput);
        newContainer.appendChild(idInput);
        container = newContainer;
    }

    // Employee ID auto-suggest
    idInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase().trim();
        if (searchTerm.length < 1) {
            clearSuggestions();
            return;
        }

        const suggestions = employeeList.filter(emp =>
            emp.id.toLowerCase().includes(searchTerm) ||
            emp.name.toLowerCase().includes(searchTerm)
        );

        showSuggestions(suggestions, this);
    });

    // Clear suggestions when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.suggestions-container')) {
            clearSuggestions();
        }
    });

    // Handle input focus
    idInput.addEventListener('focus', function() {
        const searchTerm = this.value.toLowerCase().trim();
        if (searchTerm.length >= 1) {
            const suggestions = employeeList.filter(emp =>
                emp.id.toLowerCase().includes(searchTerm) ||
                emp.name.toLowerCase().includes(searchTerm)
            );
            if (suggestions.length > 0) {
                showSuggestions(suggestions, this);
            }
        }
    });

    // Handle keyboard
    idInput.addEventListener('keydown', function(e) {
        const dropdown = document.querySelector('.suggestions-dropdown');
        if (!dropdown) return;

        const items = dropdown.querySelectorAll('.suggestion-item');
        const activeItem = dropdown.querySelector('.suggestion-item.active');

        if (e.key === 'ArrowDown' && items.length > 0) {
            e.preventDefault();
            let nextItem = items[0];
            if (activeItem) {
                const currentIndex = Array.from(items).indexOf(activeItem);
                nextItem = items[(currentIndex + 1) % items.length];
                activeItem.classList.remove('active');
            }
            nextItem.classList.add('active');
        } else if (e.key === 'ArrowUp' && items.length > 0) {
            e.preventDefault();
            let prevItem = items[items.length - 1];
            if (activeItem) {
                const currentIndex = Array.from(items).indexOf(activeItem);
                prevItem = items[(currentIndex - 1 + items.length) % items.length];
                activeItem.classList.remove('active');
            }
            prevItem.classList.add('active');
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (activeItem) {
                activeItem.click();
            }
        } else if (e.key === 'Escape') {
            clearSuggestions();
            idInput.blur();
        }
    });
}

function showSuggestions(suggestions, inputElement) {
    clearSuggestions();

    if (suggestions.length === 0) return;

    const dropdown = document.createElement('div');
    dropdown.className = 'suggestions-dropdown';

    suggestions.slice(0, 8).forEach(emp => {
        const item = document.createElement('div');
        item.className = 'suggestion-item';
        item.innerHTML = `
            <div class="employee-main">
                <span class="employee-name">${emp.name}</span>
                <span class="employee-id">${emp.id}</span>
            </div>
            <div class="employee-department">${emp.department}</div>
        `;

        item.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            inputElement.value = emp.id;
            clearSuggestions();
            inputElement.focus(); // Keep focus on input
        });

        // Add hover effect
        item.addEventListener('mouseenter', function() {
            // Remove active class from all items
            dropdown.querySelectorAll('.suggestion-item').forEach(i => {
                i.classList.remove('active');
            });
            this.classList.add('active');
        });

        dropdown.appendChild(item);
    });

    // Append dropdown to the container
    const container = inputElement.parentElement;
    container.appendChild(dropdown);
}

function clearSuggestions() {
    const existingDropdown = document.querySelector('.suggestions-dropdown');
    if (existingDropdown) {
        existingDropdown.remove();
    }
}

function showSuggestions(suggestions, inputElement) {
    clearSuggestions();

    if (suggestions.length === 0) return;

    const dropdown = document.createElement('div');
    dropdown.className = 'suggestions-dropdown';

    suggestions.slice(0, 8).forEach(emp => {
        const item = document.createElement('div');
        item.className = 'suggestion-item';
        item.innerHTML = `
            <div class="employee-main">
                <span class="employee-name">${emp.name}</span>
                <span class="employee-id">${emp.id}</span>
            </div>
            <div class="employee-department">${emp.department}</div>
        `;

        item.addEventListener('click', function(e) {
            e.stopPropagation();
            inputElement.value = emp.id;
            clearSuggestions();

            // Trigger search if you want auto-search on selection
            // searchAttendance();
        });

        // Add hover effect
        item.addEventListener('mouseenter', function() {
            this.classList.add('active');
        });

        item.addEventListener('mouseleave', function() {
            this.classList.remove('active');
        });

        dropdown.appendChild(item);
    });

    // Append dropdown to the container
    const container = inputElement.parentElement;
    container.appendChild(dropdown);
}


function clearSuggestions() {
    const existingDropdown = document.querySelector('.suggestions-dropdown');
    if (existingDropdown) {
        existingDropdown.remove();
    }
}

// Enhanced search attendance function
async function searchAttendance() {
    try {
        const employeeId = document.getElementById('employeeSearch').value.trim();
        const fromDate = document.getElementById('fromDate').value;
        const toDate = document.getElementById('toDate').value;

        // Validate dates
        if (!fromDate || !toDate) {
            showAlert('Please select both From Date and To Date', 'error');
            return;
        }

        // Show loading
        const tbody = document.querySelector('#attendanceTable tbody');
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;"><i class="fas fa-spinner fa-spin"></i> Loading...</td></tr>';

        // Build URL
        const url = `/attendance/hr/search?fromDate=${fromDate}&toDate=${toDate}` +
            (employeeId ? `&employeeId=${encodeURIComponent(employeeId)}` : '');

        console.log('Searching attendance:', url);

        const response = await fetchWithAuth(url);
        const result = await response.json();

        console.log('Search Result:', result);

        if (result.success) {
            const records = result.attendanceRecords || [];
            const summary = result.summary || {};
            const searchType = result.searchCriteria?.searchType || 'MULTIPLE';

            console.log('Search Type:', searchType);
            console.log('Summary Data:', summary);

            // Display results
            displayAttendanceRecords(records);

            // Display summary based on search type
            if (searchType === 'INDIVIDUAL') {
                // INDIVIDUAL EMPLOYEE SEARCH
                displayIndividualSummary(summary, employeeId);
            } else {
                // MULTIPLE EMPLOYEES SEARCH
                displayAllEmployeesSummary(summary);
            }

            if (records.length === 0) {
                document.getElementById('noAttendanceData').style.display = 'block';
            }

        } else {
            showAlert(result.message || 'Search failed', 'error');
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No records found</td></tr>';
        }

    } catch (error) {
        console.error('Error searching attendance:', error);
        showAlert('Error: ' + error.message, 'error');
    }
}

// Display individual employee summary
function displayIndividualSummary(summary, employeeId) {
    const summaryCard = document.getElementById('attendanceSummary');
    summaryCard.style.display = 'block';

    // Update summary fields for INDIVIDUAL employee
    document.getElementById('summaryEmpId').textContent =
        summary.employeeId || employeeId || '-';

    document.getElementById('summaryEmpName').textContent =
        summary.employeeName || '1'; // Show "1" for total employees

    document.getElementById('summaryTotalDays').textContent =
        summary.totalRecords || 0;

    document.getElementById('summaryPresentDays').textContent =
        summary.presentDays || 0;

    // CRITICAL FIX: Use individual employee's absent days
    document.getElementById('summaryAbsentDays').textContent =
        summary.absentDays || 0;

    document.getElementById('summaryWorkHours').textContent =
        (summary.totalWorkHours || 0).toFixed(2);

    document.getElementById('summaryAttendanceRate').textContent =
        (summary.attendanceRate || 0).toFixed(2) + '%';

    console.log('Displayed Individual Summary:', {
        employeeId: summary.employeeId,
        presentDays: summary.presentDays,
        absentDays: summary.absentDays,
        attendanceRate: summary.attendanceRate
    });
}

// Display all employees summary
function displayAllEmployeesSummary(summary) {
    const summaryCard = document.getElementById('attendanceSummary');
    summaryCard.style.display = 'block';

    // Update summary fields for ALL employees
    document.getElementById('summaryEmpId').textContent = 'ALL EMPLOYEES';

    document.getElementById('summaryEmpName').textContent =
        summary.totalEmployees || 0;

    document.getElementById('summaryTotalDays').textContent =
        summary.totalRecords || 0;

    document.getElementById('summaryPresentDays').textContent =
        summary.totalPresentDays || 0;

    // Use total absent days for all employees
    document.getElementById('summaryAbsentDays').textContent =
        summary.totalAbsentDays || 0;

    document.getElementById('summaryWorkHours').textContent =
        (summary.totalWorkHours || 0).toFixed(2);

    document.getElementById('summaryAttendanceRate').textContent =
        (summary.attendanceRate || 0).toFixed(2) + '%';

    console.log('Displayed All Employees Summary:', {
        totalEmployees: summary.totalEmployees,
        totalPresentDays: summary.totalPresentDays,
        totalAbsentDays: summary.totalAbsentDays,
        attendanceRate: summary.attendanceRate
    });
}

// Display attendance records in table
function displayAttendanceRecords(records) {
    const tbody = document.querySelector('#attendanceTable tbody');
    tbody.innerHTML = '';

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: #6c757d;">No attendance records found</td></tr>';
        return;
    }

    records.forEach(record => {
        const date = new Date(record.attendanceDate);
        const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });

        const row = `
            <tr>
                <td>${record.employeeId || '-'}</td>
                <td>${record.attendanceDate || '-'}</td>
                <td>${dayName}</td>
                <td>${record.checkInTime || '-'}</td>
                <td>${record.checkOutTime || '-'}</td>
                <td>${record.workHours ? record.workHours.toFixed(2) : '-'}</td>
                <td><span class="status-badge status-${(record.status || 'absent').toLowerCase()}">${record.status || 'ABSENT'}</span></td>
                <td>${record.notes || '-'}</td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

// Reset filters
function resetAttendanceFilters() {
    document.getElementById('employeeSearch').value = '';
    document.getElementById('fromDate').value = '';
    document.getElementById('toDate').value = '';
    document.getElementById('attendanceSummary').style.display = 'none';
    document.getElementById('noAttendanceData').style.display = 'none';

    const tbody = document.querySelector('#attendanceTable tbody');
    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: #6c757d;">Please select dates and click Search</td></tr>';
}

// Show alert messages
function showAlert(message, type) {
    // Implementation depends on your alert system
    console.log(`${type.toUpperCase()}: ${message}`);
    alert(message);
}


// Safe element access for summary display
// Fixed summary display function
function displayAllEmployeesAttendanceSummary(summary) {
    console.log('Displaying summary:', summary);

    const elements = {
        summaryEmpId: document.getElementById('summaryEmpId'),
        summaryEmpName: document.getElementById('summaryEmpName'),
        summaryTotalDays: document.getElementById('summaryTotalDays'),
        summaryPresentDays: document.getElementById('summaryPresentDays'),
        summaryAbsentDays: document.getElementById('summaryAbsentDays'),
        summaryWorkHours: document.getElementById('summaryWorkHours'),
        summaryAttendanceRate: document.getElementById('summaryAttendanceRate')
    };

    // Debug: Check if elements exist
    Object.keys(elements).forEach(key => {
        if (!elements[key]) {
            console.error(`Element not found: ${key}`);
        }
    });

    // Safely update elements if they exist
    if (elements.summaryEmpId) {
        elements.summaryEmpId.textContent =
            (summary.searchCriteria && summary.searchCriteria.employeeId) ?
                summary.searchCriteria.employeeId : 'ALL EMPLOYEES';
    }

    if (elements.summaryEmpName) {
        elements.summaryEmpName.textContent = `Total Employees: ${summary.totalEmployees || 0}`;
    }

    if (elements.summaryTotalDays) {
        elements.summaryTotalDays.textContent = summary.totalRecords || 0;
    }

    if (elements.summaryPresentDays) {
        elements.summaryPresentDays.textContent = summary.totalPresentDays || 0;
    }

    if (elements.summaryAbsentDays) {
        elements.summaryAbsentDays.textContent = summary.totalAbsentDays || 0;
    }

    if (elements.summaryWorkHours) {
        elements.summaryWorkHours.textContent =
            summary.totalWorkHours ? Math.round(summary.totalWorkHours * 100) / 100 : 0;
    }

    // Calculate attendance rate safely
    let attendanceRate = 0;
    if (summary.totalRecords > 0 && summary.totalPresentDays > 0) {
        attendanceRate = Math.round((summary.totalPresentDays / summary.totalRecords) * 100);
    }

    if (elements.summaryAttendanceRate) {
        elements.summaryAttendanceRate.textContent = attendanceRate + '%';
    }

    // Show the summary card
    const summaryEl = document.getElementById('attendanceSummary');
    if (summaryEl) {
        summaryEl.style.display = 'block';
        console.log('Summary card displayed');
    } else {
        console.error('Summary card element not found');
    }
}

// Display attendance details table
// Update the displayAttendanceDetails function to include Employee ID
function displayAttendanceDetails(records) {
    const tbody = document.querySelector('#attendanceTable tbody');
    if (!tbody) {
        console.error('Attendance table body not found');
        return;
    }

    tbody.innerHTML = '';

    if (!records || records.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="8" style="text-align: center; color: #6c757d;">No attendance records found</td>';
        tbody.appendChild(row);
        return;
    }

    records.forEach(record => {
        const row = document.createElement('tr');

        // Format work hours safely
        const workHours = record.workHours ?
            Math.round(record.workHours * 100) / 100 : '-';

        // Format date and get day name safely
        let dayName = '-';
        let formattedDate = record.attendanceDate || '-';

        try {
            if (record.attendanceDate) {
                const date = new Date(record.attendanceDate);
                if (!isNaN(date.getTime())) {
                    dayName = date.toLocaleDateString('en-US', { weekday: 'short' });
                    formattedDate = date.toISOString().split('T')[0];
                }
            }
        } catch (error) {
            console.error('Error formatting date:', error);
        }

        // Format time safely
        const formatTime = (time) => {
            if (!time) return '-';
            try {
                if (typeof time === 'string') {
                    // If it's already a string, return as is
                    return time.includes(':') ? time : '-';
                }
                // If it's a time object, format it
                return time.toString();
            } catch (error) {
                return '-';
            }
        };

        row.innerHTML = `
            <td>${record.employeeId || '-'}</td>
            <td>${formattedDate}</td>
            <td>${dayName}</td>
            <td>${formatTime(record.checkInTime)}</td>
            <td>${formatTime(record.checkOutTime)}</td>
            <td>${workHours}</td>
            <td>
                <span class="status-badge status-${(record.status || '').toLowerCase()}">
                    ${record.status || '-'}
                </span>
            </td>
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
    try {
        const defaultOptions = {
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            }
        };

        const mergedOptions = { ...defaultOptions, ...options };
        const response = await fetch(url, mergedOptions);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return response;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}



// Alert function
function showAlert(message, type = 'info') {
    // Remove any existing alerts
    const existingAlert = document.querySelector('.custom-alert');
    if (existingAlert) {
        existingAlert.remove();
    }

    // Create alert element
    const alertDiv = document.createElement('div');
    alertDiv.className = `custom-alert alert-${type}`;
    alertDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 5px;
        color: white;
        z-index: 10000;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        max-width: 400px;
    `;

    // Set background color based on type
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        info: '#17a2b8',
        warning: '#ffc107'
    };

    alertDiv.style.backgroundColor = colors[type] || colors.info;
    alertDiv.textContent = message;

    document.body.appendChild(alertDiv);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

// Set default dates (current month)
function setDefaultDates() {
    try {
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);

        const fromDateInput = document.getElementById('fromDate');
        const toDateInput = document.getElementById('toDate');

        if (fromDateInput) {
            fromDateInput.value = formatDate(firstDay);
        }
        if (toDateInput) {
            toDateInput.value = formatDate(today);
        }
    } catch (error) {
        console.error('Error setting default dates:', error);
    }
}

function formatDate(date) {
    if (!date) return '';

    // Handle both Date objects and string dates
    const dateObj = date instanceof Date ? date : new Date(date);

    // Check if date is valid
    if (isNaN(dateObj.getTime())) {
        console.error('Invalid date:', date);
        return '';
    }

    return dateObj.toISOString().split('T')[0];
}

function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    document.getElementById(sectionId).classList.add('active');

    // Re-initialize auto-suggest if showing attendance section
    if (sectionId === 'attendance' && employeeList.length > 0) {
        setTimeout(() => {
            setupEmployeeAutoSuggest();
        }, 300);
    }
}

// Initialize attendance section when page loads
document.addEventListener('DOMContentLoaded', function() {
    setDefaultDates();
    loadEmployeeList().then(() => {
        // Small delay to ensure DOM is ready
        setTimeout(() => {
            setupEmployeeAutoSuggest();
        }, 100);
    });

});