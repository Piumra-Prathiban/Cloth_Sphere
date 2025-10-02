// Navigation functionality
// Update the existing showSection function in hrDashboard.html
function showSection(sectionId) {
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionId).classList.add('active');
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
    // Refresh data when switching to specific sections
    if (sectionId === 'departments') {
        console.log('Switching to departments section, refreshing data...');
        loadDepartments();
    } else if (sectionId === 'employees') {
        console.log('Switching to employees section, refreshing data...');
        loadEmployees();
    } else if (sectionId === 'workload') {
        console.log('Switching to workload section, loading tasks...');
        loadProductionTasks();
    }
}

// Initialize workload section when page loads (add to existing DOMContentLoaded)
document.addEventListener('DOMContentLoaded', function() {
    console.log('HR Dashboard initialized');

    // Load both employees and departments on page load
    Promise.all([loadEmployees(), loadDepartments()]).then(() => {
        console.log('Initial data loaded successfully');

        // Set a timeout to refresh data again after a short delay to ensure everything is loaded
        setTimeout(() => {
            console.log('Refreshing data after initial load...');
            Promise.all([loadEmployees(), loadDepartments()]);
        }, 1000);

    }).catch(error => {
        console.error('Error loading initial data:', error);
    });

    // Initialize workload functionality
    if (typeof loadProductionTasks === 'function') {
        console.log('Workload assignment functionality initialized');
    }
});

// Employee management functions
let employees = [];
let departments = [];

// Add this function to handle API calls with session
async function fetchWithAuth(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: 'include' // Include session cookies
    });

    if (response.status === 401) {
        alert('Session expired. Please login again.');
        window.location.href = '/systemUserLogin';
        throw new Error('Unauthorized');
    }

    return response;
}

// Load employees from server
async function loadEmployees() {
    try {
        console.log('Loading employees...');
        const response = await fetchWithAuth('/api/employees');
        if (response.ok) {
            employees = await response.json();
            console.log('Employees loaded:', employees.length);
            refreshEmployeeTable();
        } else {
            console.error('Failed to load employees. Status:', response.status);
        }
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

async function showAddEmployeeModal() {
    document.getElementById('modalTitle').textContent = 'Add New Employee';
    document.getElementById('employeeForm').reset();
    document.getElementById('employeeId').value = '';

    // Populate department dropdown BEFORE showing modal
    await populateDepartmentDropdown();

    document.getElementById('employeeModal').style.display = 'block';

    // Auto-generate credentials when email is entered
    document.getElementById('email').addEventListener('input', generateCredentials);
}

function closeEmployeeModal() {
    document.getElementById('employeeModal').style.display = 'none';
}

async function editEmployee(id) {
    try {
        const response = await fetchWithAuth(`/api/employees/${id}`);
        if (response.ok) {
            const employee = await response.json();
            document.getElementById('modalTitle').textContent = 'Edit Employee';
            document.getElementById('employeeId').value = employee.id;
            document.getElementById('fullName').value = employee.fullName || '';
            document.getElementById('address').value = employee.address || '';
            document.getElementById('phoneNumber').value = employee.phoneNumber || '';
            document.getElementById('email').value = employee.email || '';
            document.getElementById('dateOfBirth').value = employee.dateOfBirth || '';
            document.getElementById('qualification1').value = employee.qualification1 || '';
            document.getElementById('qualification2').value = employee.qualification2 || '';
            document.getElementById('qualification3').value = employee.qualification3 || '';
            document.getElementById('username').value = employee.username || '';
            document.getElementById('password').value = employee.password || '';

            await populateDepartmentDropdown();
            if (employee.department) {
                document.getElementById('department').value = employee.department.departmentName || '';
            }

            document.getElementById('employeeModal').style.display = 'block';
        } else {
            showAlert('Failed to load employee details', 'error');
        }
    } catch (error) {
        console.error('Error loading employee:', error);
        showAlert('Error loading employee details', 'error');
    }
}

async function deleteEmployee(id) {
    if (confirm('Are you sure you want to delete this employee? This action cannot be undone.')) {
        try {
            const response = await fetchWithAuth(`/api/employees/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });
            const result = await response.json();
            if (result.success) {
                showAlert('Employee deleted successfully!', 'success');

                // Reload both employees and departments to refresh counts
                await Promise.all([loadEmployees(), loadDepartments()]);

                console.log('Employee deleted, data refreshed');
            } else {
                showAlert(result.message || 'Failed to delete employee', 'error');
            }
        } catch (error) {
            console.error('Error deleting employee:', error);
            showAlert('Error deleting employee', 'error');
        }
    }
}

function refreshEmployeeTable() {
    const tbody = document.querySelector('#employeeTable tbody');
    tbody.innerHTML = '';

    if (employees.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No employees found</td></tr>';
        return;
    }

    employees.forEach(employee => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${employee.id || 'N/A'}</td>
            <td>${employee.fullName || 'N/A'}</td>
            <td>${employee.email || 'N/A'}</td>
            <td>${employee.phoneNumber || 'N/A'}</td>
            <td>${employee.department ? employee.department.departmentName : 'N/A'}</td>
            <td>${employee.username || 'N/A'}</td>
            <td>
              <button class="btn btn-warning btn-sm" onclick="editEmployee('${employee.id}')">
                <i class="fas fa-edit"></i> Edit
              </button>
              <button class="btn btn-danger btn-sm" onclick="deleteEmployee('${employee.id}')">
                <i class="fas fa-trash"></i> Delete
              </button>
            </td>
          `;
        tbody.appendChild(row);
    });
}

async function populateDepartmentDropdown() {
    const select = document.getElementById('department');
    select.innerHTML = '<option value="">Select Department</option>';

    try {
        const response = await fetchWithAuth('/api/departments');
        if (response.ok) {
            const depts = await response.json();
            console.log('Departments for dropdown:', depts);

            depts.forEach(department => {
                const option = document.createElement('option');
                option.value = department.departmentName;
                option.textContent = department.departmentName;
                option.setAttribute('data-id', department.id); // Store department ID
                select.appendChild(option);
            });

            // If there's only one department, select it by default
            if (depts.length === 1) {
                select.value = depts[0].departmentName;
            }
        } else {
            console.error('Failed to load departments for dropdown. Status:', response.status);
            showAlert('Failed to load departments', 'error');
        }
    } catch (error) {
        console.error('Error loading departments for dropdown:', error);
        showAlert('Error loading departments: ' + error.message, 'error');
    }
}

function generateCredentials() {
    const email = document.getElementById('email').value;
    if (email) {
        const username = email.split('@')[0];
        const defaultPassword = "changeme123"; // Use default password instead of random
        document.getElementById('username').value = username;
        document.getElementById('password').value = defaultPassword;
    }
}

function generateRandomPassword() {
    // This is now only used for other purposes, not employee creation
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
    let password = '';
    for (let i = 0; i < 12; i++) {
        password += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return password;
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'success' ? 'success' : 'error'}`;
    alertDiv.textContent = message;
    const main = document.querySelector('.main');
    main.insertBefore(alertDiv, main.children[1]);
    setTimeout(() => alertDiv.remove(), 5000);
}

// Form handling for employees
document.getElementById('email').addEventListener('input', generateCredentials);

document.getElementById('employeeForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const formData = new FormData(e.target);

    // Get the selected department
    const departmentSelect = document.getElementById('department');
    const selectedDepartment = departmentSelect.value;

    const employeeData = {
        fullName: formData.get('fullName'),
        address: formData.get('address'),
        phoneNumber: formData.get('phoneNumber'),
        email: formData.get('email'),
        dateOfBirth: formData.get('dateOfBirth'),
        qualification1: formData.get('qualification1'),
        qualification2: formData.get('qualification2'),
        qualification3: formData.get('qualification3'),
        department: selectedDepartment // Send department name
    };

    console.log('Submitting employee data:', employeeData);

    const employeeId = formData.get('employeeId');

    try {
        let response;
        if (employeeId) {
            response = await fetchWithAuth(`/api/employees/${employeeId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(employeeData)
            });
        } else {
            response = await fetchWithAuth('/api/employees', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(employeeData)
            });
        }

        const result = await response.json();
        console.log('Server response:', result);

        if (result.success) {
            if (employeeId) {
                showAlert('Employee updated successfully!', 'success');
            } else {
                // Show detailed success message for new employee with correct password
                const successMessage = `Employee added successfully! 
                    Employee ID: ${result.employeeId}
                    Username: ${result.username}
                    Default Password: ${result.password}
                    (Employee must change this on first login)`;
                showAlert(successMessage, 'success');
            }
            closeEmployeeModal();

            // Reload both employees and departments to refresh counts
            await Promise.all([loadEmployees(), loadDepartments()]);

            console.log('Employee operation completed, data refreshed');
        } else {
            showAlert(result.message || 'Operation failed', 'error');
        }
    } catch (error) {
        console.error('Error saving employee:', error);
        showAlert('Error saving employee: ' + error.message, 'error');
    }
});

// Department management functions
async function loadDepartments() {
    try {
        console.log('Loading departments...');
        const response = await fetchWithAuth('/api/departments');
        if (response.ok) {
            const text = await response.text();
            console.log('Raw response:', text);

            try {
                departments = JSON.parse(text);
                console.log('Departments loaded:', departments.length);

                // Log each department's employee count for debugging
                departments.forEach(dept => {
                    console.log(`Department ${dept.departmentName}: ${dept.employeeCount} employees`);
                });

                refreshDepartmentTable();
            } catch (parseError) {
                console.error('Error parsing JSON:', parseError);
                console.error('Response text:', text);
                showAlert('Error parsing department data', 'error');
            }
        } else {
            console.error('Failed to load departments. Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);
            showAlert('Failed to load departments: ' + response.status, 'error');
        }
    } catch (error) {
        console.error('Error loading departments:', error);
        showAlert('Error loading departments: ' + error.message, 'error');
    }
}

function showAddDepartmentModal() {
    document.getElementById('departmentModalTitle').textContent = 'Add New Department';
    document.getElementById('departmentForm').reset();
    document.getElementById('departmentId').value = '';
    document.getElementById('departmentModal').style.display = 'block';
}

function closeDepartmentModal() {
    document.getElementById('departmentModal').style.display = 'none';
}

function refreshDepartmentTable() {
    const tbody = document.querySelector('#departmentTable tbody');
    tbody.innerHTML = '';

    if (departments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No departments found</td></tr>';
        return;
    }

    departments.forEach(department => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${department.id || 'N/A'}</td>
            <td>${department.departmentName || 'N/A'}</td>
            <td>${department.description || 'No description'}</td>
            <td><strong>${department.employeeCount || 0}</strong></td>
            <td>${department.salaryBudget ? '$' + department.salaryBudget.toLocaleString() : 'N/A'}</td>
            <td>${department.managerId || 'N/A'}</td>
            <td>
              <button class="btn btn-warning btn-sm" onclick="editDepartment('${department.id}')">
                <i class="fas fa-edit"></i> Edit
              </button>
              <button class="btn btn-danger btn-sm" onclick="deleteDepartment('${department.id}')">
                <i class="fas fa-trash"></i> Delete
              </button>
            </td>
          `;
        tbody.appendChild(row);
    });

    console.log('Department table refreshed with', departments.length, 'departments');
}

async function editDepartment(id) {
    try {
        const response = await fetchWithAuth(`/api/departments/${id}`);
        if (response.ok) {
            const department = await response.json();
            document.getElementById('departmentModalTitle').textContent = 'Edit Department';
            document.getElementById('departmentId').value = department.id;
            document.getElementById('departmentName').value = department.departmentName || '';
            document.getElementById('description').value = department.description || '';
            document.getElementById('salaryBudget').value = department.salaryBudget || '';
            document.getElementById('managerId').value = department.managerId || '';
            document.getElementById('departmentModal').style.display = 'block';
        } else {
            showAlert('Failed to load department details', 'error');
        }
    } catch (error) {
        console.error('Error loading department:', error);
        showAlert('Error loading department details', 'error');
    }
}

async function deleteDepartment(id) {
    if (confirm('Are you sure you want to delete this department? This action cannot be undone.')) {
        try {
            const response = await fetchWithAuth(`/api/departments/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });
            const result = await response.json();
            if (result.success) {
                showAlert('Department deleted successfully!', 'success');
                await loadDepartments();
                await loadEmployees();
            } else {
                showAlert(result.message || 'Failed to delete department', 'error');
            }
        } catch (error) {
            console.error('Error deleting department:', error);
            showAlert('Error deleting department', 'error');
        }
    }
}

document.getElementById('departmentForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const departmentData = {
        departmentName: formData.get('departmentName'),
        description: formData.get('description'),
        salaryBudget: formData.get('salaryBudget') ? parseFloat(formData.get('salaryBudget')) : null,
        managerId: formData.get('managerId') || null
    };

    const departmentId = formData.get('departmentId');

    try {
        let response;
        if (departmentId) {
            response = await fetchWithAuth(`/api/departments/${departmentId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(departmentData)
            });
        } else {
            response = await fetchWithAuth('/api/departments', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(departmentData)
            });
        }

        const result = await response.json();
        if (result.success) {
            showAlert(departmentId ? 'Department updated successfully!' : 'Department created successfully!', 'success');
            closeDepartmentModal();
            await loadDepartments();
        } else {
            showAlert(result.message || 'Operation failed', 'error');
        }
    } catch (error) {
        console.error('Error saving department:', error);
        showAlert('Error saving department', 'error');
    }
});

// Attendance Chart
const ctx = document.getElementById('attendanceChart');
if (ctx) {
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Working Employees', 'On Leave'],
            datasets: [{ data: [85, 15], backgroundColor: ['#28a745', '#dc3545'] }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
}

// Other functions
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = '/systemUserLogin';
    }
}

function resetForm() {
    document.getElementById('passwordForm').reset();
}

document.getElementById('passwordForm').addEventListener('submit', function(e) {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    if (newPassword !== confirmPassword) {
        e.preventDefault();
        alert('New password and confirmation do not match!');
        return false;
    }
    if (newPassword.length < 6) {
        e.preventDefault();
        alert('Password must be at least 6 characters long!');
        return false;
    }
    return true;
});

window.onclick = function(event) {
    const modal = document.getElementById('employeeModal');
    if (event.target === modal) closeEmployeeModal();
    const departmentModal = document.getElementById('departmentModal');
    if (event.target === departmentModal) closeDepartmentModal();
};

// Handle zoom and resize events
window.addEventListener('resize', handleResize);

function handleResize() {
    const zoomLevel = detectZoomLevel();

    if (zoomLevel < 80) {
        // Apply compact layout for high zoom out
        document.body.classList.add('zoomed-out');
    } else {
        document.body.classList.remove('zoomed-out');
    }
}

function detectZoomLevel() {
    // Detect browser zoom level
    const width = window.innerWidth;
    const height = window.innerHeight;
    const normalWidth = screen.width;

    // Calculate approximate zoom level
    const zoomLevel = Math.round((width / normalWidth) * 100);
    return zoomLevel;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    handleResize();

    // Your existing initialization code...
    console.log('HR Dashboard initialized');
    Promise.all([loadEmployees(), loadDepartments()]).then(() => {
        console.log('Initial data loaded successfully');
        setTimeout(() => {
            console.log('Refreshing data after initial load...');
            Promise.all([loadEmployees(), loadDepartments()]);
        }, 1000);
    }).catch(error => {
        console.error('Error loading initial data:', error);
    });
// Function to update progress bars with real data from API
    async function updateWorkloadProgress() {
        try {
            console.log('Fetching workload statistics...');
            const response = await fetchWithAuth('/api/assignments/statistics');

            if (response.ok) {
                const stats = await response.json();
                console.log('Workload stats received:', stats);

                // Calculate percentages safely
                const totalAssignments = stats.totalAssignments || 0;

                if (totalAssignments === 0) {
                    // Show 0% for all if no assignments exist
                    updateProgressBar('.progress.finish', 0);
                    updateProgressBar('.progress.in-progress', 0);
                    updateProgressBar('.progress.pending', 0);
                    updateSummaryText(0, 0, 0);
                    return;
                }

                const completedPercentage = Math.round((stats.completedTasks / totalAssignments) * 100);
                const inProgressPercentage = Math.round((stats.inProgressTasks / totalAssignments) * 100);
                const assignedPercentage = Math.round((stats.assignedTasks / totalAssignments) * 100);

                console.log('Calculated percentages:', {
                    completed: completedPercentage,
                    inProgress: inProgressPercentage,
                    assigned: assignedPercentage
                });

                // Update progress bars
                updateProgressBar('.progress.finish', completedPercentage);
                updateProgressBar('.progress.in-progress', inProgressPercentage);
                updateProgressBar('.progress.pending', assignedPercentage);

                // Update summary text
                updateSummaryText(completedPercentage, inProgressPercentage, assignedPercentage);

            } else {
                console.error('Failed to fetch workload statistics:', response.status);
            }
        } catch (error) {
            console.error('Error updating workload progress:', error);
        }
    }

    // Helper function to update a single progress bar
    function updateProgressBar(selector, percentage) {
        const progressBar = document.querySelector(selector);
        if (progressBar) {
            progressBar.style.width = percentage + '%';
            progressBar.textContent = percentage + '%';
        }
    }

    // Helper function to update summary text
    function updateSummaryText(completed, inProgress, pending) {
        const summaryElement = document.querySelector('.card h3 + p + div.progress-bar + p + div.progress-bar + p + div.progress-bar + div small');
        if (summaryElement) {
            summaryElement.innerHTML = `<strong>Summary:</strong> ${completed}% Completed • ${inProgress}% In Progress • ${pending}% Pending`;
        }
    }

    // Initial call when dashboard loads
    updateWorkloadProgress();

    // Refresh every 30 seconds
    setInterval(updateWorkloadProgress, 30000);

// Call this function when the dashboard loads
    document.addEventListener('DOMContentLoaded', function() {
        // Your existing initialization code...
        updateWorkloadProgress();

        // Optionally update progress every 30 seconds
        setInterval(updateWorkloadProgress, 30000);
    });

    // ==============================================
// CALENDAR FUNCTIONALITY - STANDALONE
// Add this to the END of your hrDashboard.js file
// ==============================================

// Calendar variables
    const monthNames = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"];
    const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    let currentDate = new Date();
    let selectedDate = null;
    let notes = {};

// Load notes for the visible calendar month
    async function loadNotesForMonth(year, month) {
        try {
            const startDate = new Date(year, month, 1);
            const endDate = new Date(year, month + 1, 0);

            const startDateStr = startDate.toISOString().split('T')[0];
            const endDateStr = endDate.toISOString().split('T')[0];

            const response = await fetchWithAuth(`/api/calendar-notes/range/${startDateStr}/${endDateStr}`);
            if (response.ok) {
                const notesArray = await response.json();
                notes = {};
                notesArray.forEach(note => {
                    const dateStr = note.noteDate;
                    if (!notes[dateStr]) {
                        notes[dateStr] = [];
                    }
                    notes[dateStr].push(note.noteText);
                });
                console.log('Notes loaded for month:', notes);
            }
        } catch (error) {
            console.error('Error loading notes:', error);
        }
    }

    function renderCalendar() {
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();

        document.getElementById('currentMonth').textContent = `${monthNames[month]} ${year}`;

        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const daysInPrevMonth = new Date(year, month, 0).getDate();

        const calendarGrid = document.getElementById('calendarGrid');
        calendarGrid.innerHTML = '';

        // Add day headers
        dayNames.forEach(day => {
            const dayHeader = document.createElement('div');
            dayHeader.className = 'calendar-day-header';
            dayHeader.textContent = day;
            calendarGrid.appendChild(dayHeader);
        });

        // Previous month days
        for (let i = firstDay - 1; i >= 0; i--) {
            const day = daysInPrevMonth - i;
            const dayEl = createDayElement(day, true, year, month - 1);
            calendarGrid.appendChild(dayEl);
        }

        // Current month days
        for (let day = 1; day <= daysInMonth; day++) {
            const dayEl = createDayElement(day, false, year, month);
            calendarGrid.appendChild(dayEl);
        }

        // Next month days
        const remainingDays = 42 - (firstDay + daysInMonth);
        for (let day = 1; day <= remainingDays; day++) {
            const dayEl = createDayElement(day, true, year, month + 1);
            calendarGrid.appendChild(dayEl);
        }
    }

    function createDayElement(day, isOtherMonth, year, month) {
        const dayEl = document.createElement('div');
        dayEl.className = 'calendar-day';
        dayEl.textContent = day;

        if (isOtherMonth) {
            dayEl.classList.add('other-month');
        }

        const dateStr = getDateString(year, month, day);

        if (!isOtherMonth && isToday(year, month, day)) {
            dayEl.classList.add('today');
        }

        if (notes[dateStr] && notes[dateStr].length > 0) {
            dayEl.classList.add('has-note');
        }

        dayEl.addEventListener('click', () => selectDate(year, month, day));

        return dayEl;
    }

    function isToday(year, month, day) {
        const today = new Date();
        return today.getFullYear() === year &&
            today.getMonth() === month &&
            today.getDate() === day;
    }

    function getDateString(year, month, day) {
        const date = new Date(year, month, day);
        return date.toISOString().split('T')[0];
    }

    async function selectDate(year, month, day) {
        selectedDate = new Date(year, month, day);
        const dateStr = getDateString(year, month, day);

        const dateDisplay = selectedDate.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });

        document.getElementById('selectedDate').textContent = dateDisplay;
        document.getElementById('noteInput').disabled = false;
        document.getElementById('addNoteBtn').disabled = false;

        await renderNotes(dateStr);
    }

    async function renderNotes(dateStr) {
        const notesList = document.getElementById('notesList');

        try {
            const response = await fetchWithAuth(`/api/calendar-notes/${dateStr}`);
            if (response.ok) {
                const notesArray = await response.json();
                const dateNotes = notesArray.map(note => note.noteText);

                if (dateNotes.length === 0) {
                    notesList.innerHTML = '<div class="no-notes">No notes for this date</div>';
                    return;
                }

                notesList.innerHTML = '';
                dateNotes.forEach(note => {
                    const noteItem = document.createElement('div');
                    noteItem.className = 'note-item';

                    const noteText = document.createElement('div');
                    noteText.className = 'note-text';
                    noteText.textContent = note;

                    const deleteBtn = document.createElement('button');
                    deleteBtn.className = 'delete-note-btn';
                    deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
                    deleteBtn.onclick = () => deleteNote(dateStr, note);

                    noteItem.appendChild(noteText);
                    noteItem.appendChild(deleteBtn);
                    notesList.appendChild(noteItem);
                });
            } else {
                notesList.innerHTML = '<div class="no-notes">Error loading notes</div>';
            }
        } catch (error) {
            console.error('Error rendering notes:', error);
            notesList.innerHTML = '<div class="no-notes">Error loading notes</div>';
        }
    }

// In hrDashboard.js - Update the addNote function
    async function addNote() {
        console.log('=== ADD NOTE FUNCTION CALLED ===');

        if (!selectedDate) {
            console.log('No date selected');
            showAlert('Please select a date first', 'error');
            return;
        }

        const noteInput = document.getElementById('noteInput');
        const noteText = noteInput.value.trim();

        console.log('Note text:', noteText);

        if (!noteText) {
            showAlert('Please enter a note', 'error');
            return;
        }

        const dateStr = selectedDate.toISOString().split('T')[0];
        console.log('Saving note for date:', dateStr);

        try {
            const response = await fetchWithAuth('/api/calendar-notes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    noteDate: dateStr,
                    noteText: noteText
                })
            });

            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);

            const result = await response.json();
            console.log('Server response:', result);

            if (response.ok && result.success) {
                console.log('Note added successfully');
                noteInput.value = '';

                // Refresh the notes list and calendar
                await renderNotes(dateStr);
                await loadNotesForMonth(currentDate.getFullYear(), currentDate.getMonth());
                renderCalendar();

                showAlert('Note added successfully!', 'success');
            } else {
                console.error('Failed to add note:', result.message);
                showAlert(result.message || 'Failed to add note', 'error');
            }
        } catch (error) {
            console.error('Error adding note:', error);
            showAlert('Error adding note: ' + error.message, 'error');
        }
    }

    async function deleteNote(dateStr, noteText) {
        try {
            const response = await fetchWithAuth(`/api/calendar-notes?date=${dateStr}&noteText=${encodeURIComponent(noteText)}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    await renderNotes(dateStr);
                    await loadNotesForMonth(currentDate.getFullYear(), currentDate.getMonth());
                    renderCalendar();
                    showAlert('Note deleted successfully!', 'success');
                } else {
                    showAlert(result.message || 'Failed to delete note', 'error');
                }
            } else {
                showAlert('Failed to delete note', 'error');
            }
        } catch (error) {
            console.error('Error deleting note:', error);
            showAlert('Error deleting note: ' + error.message, 'error');
        }
    }

// Initialize calendar
    function initCalendar() {
        console.log('=== INITIALIZING CALENDAR ===');

        // Load notes and render calendar
        loadNotesForMonth(currentDate.getFullYear(), currentDate.getMonth()).then(() => {
            renderCalendar();
        });

        // Navigation buttons
        const prevBtn = document.getElementById('prevMonth');
        const nextBtn = document.getElementById('nextMonth');

        if (prevBtn) {
            prevBtn.onclick = async () => {
                currentDate.setMonth(currentDate.getMonth() - 1);
                await loadNotesForMonth(currentDate.getFullYear(), currentDate.getMonth());
                renderCalendar();
            };
        }

        if (nextBtn) {
            nextBtn.onclick = async () => {
                currentDate.setMonth(currentDate.getMonth() + 1);
                await loadNotesForMonth(currentDate.getFullYear(), currentDate.getMonth());
                renderCalendar();
            };
        }

        // Add note button
        const addBtn = document.getElementById('addNoteBtn');
        if (addBtn) {
            console.log('Attaching click event to add button');
            addBtn.onclick = function() {
                console.log('Button clicked via onclick');
                addNote();
            };
        } else {
            console.error('Add note button not found!');
        }

        // Enter key support
        const noteInput = document.getElementById('noteInput');
        if (noteInput) {
            noteInput.onkeypress = (e) => {
                if (e.key === 'Enter') {
                    addNote();
                }
            };
        }

        console.log('=== CALENDAR INITIALIZATION COMPLETE ===');
    }

// Call initCalendar after a delay to ensure DOM is ready
    setTimeout(() => {
        console.log('Starting calendar initialization...');
        initCalendar();
    }, 1000);

});