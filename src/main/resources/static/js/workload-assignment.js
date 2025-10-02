// Workload Assignment JavaScript
let allTasks = [];
let allAssignments = [];
let allEmployees = [];
let taskDepartments = [];

function showTab(tabName, event) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.getElementById(tabName).classList.add('active');

    // Find and activate the correct tab button
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        if (tab.onclick && tab.onclick.toString().includes(tabName)) {
            tab.classList.add('active');
        }
    });

    if (tabName === 'taskOverview') {
        loadProductionTasks();
    } else if (tabName === 'assignWork') {
        loadEmployeesForAssignment();
        loadTasksForAssignment();
    } else if (tabName === 'assignmentOverview') {
        loadAssignments();
    }
}

async function fetchWithAuth(url, options = {}) {
    console.log('Making API request to:', url, 'with options:', options);
    const response = await fetch(url, {...options, credentials:'include'});
    console.log('API response status:', response.status);

    if (response.status === 401) {
        alert('Session expired. Please login again.');
        window.location.href = '/systemUserLogin';
        throw new Error('Unauthorized');
    }
    return response;
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `<span>${message}</span>
    <button onclick="this.parentElement.remove()" style="float:right; background:none; border:none; cursor:pointer;">×</button>`;
    document.getElementById('alertContainer').appendChild(alertDiv);
    setTimeout(() => alertDiv.remove(), 5000);
}

function showNotification(message, type) {
    showAlert(message, type);
}

function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<tr><td colspan="9" style="text-align:center;"><div class="spinner"></div>Loading...</td></tr>';
    }
}

async function loadProductionTasks() {
    const loadingEl = document.getElementById('loadingTasks');
    const tbody = document.querySelector('#tasksTable tbody');

    if (loadingEl) loadingEl.style.display = 'block';
    if (tbody) tbody.innerHTML = '';

    try {
        console.log('Loading production tasks...');
        const response = await fetchWithAuth('/api/workload/tasks');

        if (response.ok) {
            allTasks = await response.json();
            console.log('Loaded', allTasks.length, 'production tasks');

            // Load departments for filter
            await loadDepartmentsForTasks();

            refreshTasksTable();
            updateTaskStatistics();
        } else {
            console.error('Failed to load tasks. Status:', response.status);
            showAlert('Failed to load production tasks', 'error');
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
        showAlert('Error loading tasks: ' + error.message, 'error');
    } finally {
        if (loadingEl) loadingEl.style.display = 'none';
    }
}

async function loadDepartmentsForTasks() {
    try {
        const response = await fetchWithAuth('/api/departments');
        if (response.ok) {
            taskDepartments = await response.json();

            // Populate department filter
            const departmentFilter = document.getElementById('departmentFilter');
            const taskDepartmentSelect = document.getElementById('taskDepartment');
            const editTaskDepartmentSelect = document.getElementById('editTaskDepartment');

            if (departmentFilter) {
                departmentFilter.innerHTML = '<option value="">All Departments</option>';
                taskDepartments.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.id;
                    option.textContent = dept.departmentName;
                    departmentFilter.appendChild(option);
                });
            }

            if (taskDepartmentSelect) {
                taskDepartmentSelect.innerHTML = '<option value="">Select Department</option>';
                taskDepartments.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.id;
                    option.textContent = `${dept.departmentName} (${dept.id})`;
                    taskDepartmentSelect.appendChild(option);
                });
            }

            if (editTaskDepartmentSelect) {
                editTaskDepartmentSelect.innerHTML = '<option value="">Select Department</option>';
                taskDepartments.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.id;
                    option.textContent = `${dept.departmentName} (${dept.id})`;
                    editTaskDepartmentSelect.appendChild(option);
                });
            }
        }
    } catch (error) {
        console.error('Error loading departments for tasks:', error);
    }
}

function refreshTasksTable() {
    const tbody = document.querySelector('#tasksTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (allTasks.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No production tasks found</td></tr>';
        return;
    }

    // Apply filters
    let filteredTasks = applyTaskFilters(allTasks);

    filteredTasks.forEach(task => {
        const row = document.createElement('tr');

        // Check if task is overdue
        const isOverdue = new Date(task.deadline) < new Date() &&
            task.status !== 'COMPLETED' && task.status !== 'CANCELLED';
        if (isOverdue) {
            row.classList.add('overdue');
        }

        row.innerHTML = `
        <td><strong>${task.taskId}</strong></td>
        <td>
          <div style="font-weight: 600;">${task.taskName}</div>
          ${task.description ? `<small style="color: #666;">${task.description.substring(0, 50)}${task.description.length > 50 ? '...' : ''}</small>` : ''}
        </td>
        <td>${task.department ? task.department.departmentName : 'N/A'}</td>
        <td><span class="priority-badge priority-${task.priority.toLowerCase()}">${task.priority} - ${getPriorityLabel(task.priority)}</span></td>
        <td><span class="status-badge status-${task.status.toLowerCase()}">${formatStatus(task.status)}</span></td>
        <td>
          <div style="font-weight: 600;">${formatDate(task.deadline)}</div>
          ${isOverdue ? '<small style="color: #d32f2f; font-weight: 600;">OVERDUE</small>' : ''}
        </td>
        <td>${formatDate(task.createdDate)}</td>
        <td>
          <button class="btn btn-warning btn-sm" onclick="editTask('${task.taskId}')">
            <i class="fas fa-edit"></i> Edit
          </button>
          <button class="btn btn-danger btn-sm" onclick="deleteTask('${task.taskId}')">
            <i class="fas fa-trash"></i> Delete
          </button>
        </td>
      `;
        tbody.appendChild(row);
    });
}

function applyTaskFilters(tasks) {
    const statusFilter = document.getElementById('statusFilter')?.value || '';
    const priorityFilter = document.getElementById('priorityFilter')?.value || '';
    const departmentFilter = document.getElementById('departmentFilter')?.value || '';
    const searchQuery = document.getElementById('searchTasks')?.value?.toLowerCase() || '';

    return tasks.filter(task => {
        if (statusFilter && task.status !== statusFilter) return false;
        if (priorityFilter && task.priority !== priorityFilter) return false;
        if (departmentFilter && task.department?.id !== departmentFilter) return false;
        if (searchQuery && !task.taskName.toLowerCase().includes(searchQuery) &&
            !(task.description && task.description.toLowerCase().includes(searchQuery))) return false;

        return true;
    });
}

function filterTasks() {
    refreshTasksTable();
}

function searchTasks() {
    refreshTasksTable();
}

function getPriorityLabel(priority) {
    const labels = {
        'P0': 'Critical',
        'P1': 'High',
        'P2': 'Medium',
        'P3': 'Low'
    };
    return labels[priority] || priority;
}

function formatStatus(status) {
    return status.replace('_', ' ').charAt(0).toUpperCase() + status.replace('_', ' ').slice(1).toLowerCase();
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

async function updateTaskStatistics() {
    try {
        const response = await fetchWithAuth('/api/workload/statistics');
        if (response.ok) {
            const stats = await response.json();

            document.getElementById('totalTasksCount').textContent = stats.totalTasks || 0;
            document.getElementById('pendingTasksCount').textContent = stats.pendingTasks || 0;
            document.getElementById('completedTasksCount').textContent = stats.completedTasks || 0;
            document.getElementById('overdueTasksCount').textContent = stats.overdueTasks || 0;
        }
    } catch (error) {
        console.error('Error loading task statistics:', error);
    }
}

async function editTask(taskId) {
    try {
        const response = await fetchWithAuth(`/api/workload/tasks/${taskId}`);
        if (response.ok) {
            const task = await response.json();

            document.getElementById('editTaskId').value = task.taskId;
            document.getElementById('editTaskName').value = task.taskName || '';
            document.getElementById('editTaskDescription').value = task.description || '';
            document.getElementById('editTaskPriority').value = task.priority || '';
            document.getElementById('editTaskStatus').value = task.status || '';
            document.getElementById('editTaskDeadline').value = task.deadline || '';

            if (task.department) {
                document.getElementById('editTaskDepartment').value = task.department.id;
            }

            document.getElementById('editTaskModal').style.display = 'block';
        } else {
            showAlert('Failed to load task details', 'error');
        }
    } catch (error) {
        console.error('Error loading task:', error);
        showAlert('Error loading task details', 'error');
    }
}

function closeEditTaskModal() {
    document.getElementById('editTaskModal').style.display = 'none';
}

async function deleteTask(taskId) {
    if (confirm('Are you sure you want to delete this production task? This action cannot be undone.')) {
        try {
            const response = await fetchWithAuth(`/api/workload/tasks/${taskId}`, {
                method: 'DELETE'
            });

            const result = await response.json();
            if (result.success) {
                showAlert('Task deleted successfully!', 'success');
                await loadProductionTasks();
            } else {
                showAlert(result.message || 'Failed to delete task', 'error');
            }
        } catch (error) {
            console.error('Error deleting task:', error);
            showAlert('Error deleting task', 'error');
        }
    }
}

// Assignment Management Functions
async function loadAssignments() {
    try {
        const tbody = document.getElementById('assignmentsTableBody');
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;"><div class="spinner"></div>Loading assignments...</td></tr>';

        // Load statistics first
        try {
            const statsResponse = await fetchWithAuth('/api/assignments/statistics');
            if (statsResponse.ok) {
                const stats = await statsResponse.json();
                document.getElementById('totalAssignmentsCount').textContent = stats.totalAssignments || 0;
                document.getElementById('assignedTasksCount').textContent = stats.assignedTasks || 0;
                document.getElementById('completedAssignmentsCount').textContent = stats.completedTasks || 0;
                document.getElementById('inProgressAssignmentsCount').textContent = stats.inProgressTasks || 0;
            }
        } catch (statsError) {
            console.warn('Could not load statistics:', statsError);
        }

        // Load all assignments
        const response = await fetchWithAuth('/api/assignments');

        if (response.ok) {
            allAssignments = await response.json();
            console.log('Loaded assignments with full structure:', allAssignments);

            // Debug: Check the structure of the first assignment
            if (allAssignments.length > 0) {
                console.log('First assignment structure:', allAssignments[0]);
                console.log('Employee department:', allAssignments[0].employee?.department);
                console.log('Task department:', allAssignments[0].task?.department);
            }

            // FIXED: Always display assignments and populate filters
            displayAssignments(allAssignments);

            // Load employees and tasks for filters
            await loadEmployeesForAssignment();
            await loadTasksForAssignment();
            populateFilters();
        } else {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

    } catch (error) {
        console.error('Error loading assignments:', error);
        const tbody = document.getElementById('assignmentsTableBody');
        tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; color:red;">Error loading assignments: ${error.message}</td></tr>`;
        showNotification('Error loading assignments: ' + error.message, 'error');
    }
}

function displayAssignments(assignments) {
    const tbody = document.getElementById('assignmentsTableBody');
    tbody.innerHTML = '';

    if (assignments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">No assignments found</td></tr>';
        return;
    }

    assignments.forEach(assignment => {
        const row = document.createElement('tr');

        // Get department name directly from assignment's department
        let departmentName = 'N/A';
        if (assignment.department && assignment.department.departmentName) {
            departmentName = assignment.department.departmentName;
        }

        row.innerHTML = `
            <td><strong>${assignment.assignmentId}</strong></td>
            <td>
                <div style="font-weight: 600;">${assignment.employee ? assignment.employee.fullName : 'N/A'}</div>
                ${assignment.employee ? `<small style="color: #666;">ID: ${assignment.employee.id}</small>` : ''}
            </td>
            <td>
                <div style="font-weight: 600;">${assignment.task ? assignment.task.taskName : 'N/A'}</div>
                ${assignment.task ? `<small style="color: #666;">ID: ${assignment.task.taskId}</small>` : ''}
            </td>
            <td><span class="department-name">${departmentName}</span></td>
            <td>${formatDate(assignment.assignedDate)}</td>
            <td>${assignment.task ? formatDate(assignment.task.deadline) : 'N/A'}</td>
            <td><span class="status-badge status-${assignment.status.toLowerCase()}">${assignment.statusLabel || formatStatus(assignment.status)}</span></td>
            <td>${assignment.estimatedHours || 'N/A'}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewAssignmentDetails('${assignment.assignmentId}')" title="View Details">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-sm btn-warning" onclick="editAssignment('${assignment.assignmentId}')" title="Edit">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteAssignment('${assignment.assignmentId}')" title="Delete">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function filterAssignments() {
    const statusFilter = document.getElementById('assignmentStatusFilter').value;
    const employeeFilter = document.getElementById('assignmentEmployeeFilter').value;
    const taskFilter = document.getElementById('assignmentTaskFilter').value;

    const filtered = allAssignments.filter(assignment => {
        const statusMatch = !statusFilter || assignment.status === statusFilter;
        const employeeMatch = !employeeFilter || (assignment.employee && assignment.employee.id === employeeFilter);
        const taskMatch = !taskFilter || (assignment.task && assignment.task.taskId === taskFilter);

        return statusMatch && employeeMatch && taskMatch;
    });

    if (filtered.length === 0) {
        displayNoAssignmentsMessage();
    } else {
        displayAssignments(filtered);
    }
}

function populateFilters() {
    const employeeFilter = document.getElementById('assignmentEmployeeFilter');
    const taskFilter = document.getElementById('assignmentTaskFilter');

    // Clear existing options except first
    employeeFilter.innerHTML = '<option value="">All Employees</option>';
    taskFilter.innerHTML = '<option value="">All Tasks</option>';

    // Add unique employees
    const uniqueEmployees = [...new Set(allAssignments.map(a => a.employee ? a.employee.id : null).filter(id => id))];
    uniqueEmployees.forEach(empId => {
        const employee = allEmployees.find(e => e.id === empId);
        if (employee) {
            const option = document.createElement('option');
            option.value = empId;
            option.textContent = employee.fullName;
            employeeFilter.appendChild(option);
        }
    });

    // Add unique tasks
    const uniqueTasks = [...new Set(allAssignments.map(a => a.task ? a.task.taskId : null).filter(id => id))];
    uniqueTasks.forEach(taskId => {
        const task = allTasks.find(t => t.taskId === taskId);
        if (task) {
            const option = document.createElement('option');
            option.value = taskId;
            option.textContent = task.taskName;
            taskFilter.appendChild(option);
        }
    });
}

async function loadEmployeesForAssignment() {
    try {
        const response = await fetchWithAuth('/api/employees');
        if (response.ok) {
            allEmployees = await response.json();
            populateEmployeeSelection();
            populateDepartmentFilter();
        }
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

async function loadTasksForAssignment() {
    try {
        const response = await fetchWithAuth('/api/workload/tasks');
        if (response.ok) {
            allTasks = await response.json();
            populateTaskSelection();
        } else {
            throw new Error('Failed to load tasks');
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
        showNotification('Error loading tasks: ' + error.message, 'error');
    }
}

function populateEmployeeSelection() {
    const container = document.getElementById('employeeSelection');
    container.innerHTML = '';

    if (allEmployees.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No employees found</p>';
        return;
    }

    allEmployees.forEach(employee => {
        const div = document.createElement('div');
        div.className = 'employee-checkbox';
        div.innerHTML = `
        <label style="display: flex; align-items: center; gap: 10px; padding: 8px; border: 1px solid #eee; border-radius: 5px; margin: 5px 0;">
            <input type="checkbox" name="employeeIds" value="${employee.id}"
                onchange="updateEmployeeSelection()">
            <div style="flex: 1;">
                <div style="font-weight: 600;">${employee.fullName}</div>
                <div style="font-size: 12px; color: #666;">
                    ID: ${employee.id} |
                    Department: ${employee.department ? employee.department.departmentName : 'No Department'} |
                    Email: ${employee.email || 'N/A'}
                </div>
            </div>
        </label>
    `;
        container.appendChild(div);
    });
}

function populateDepartmentFilter() {
    const select = document.getElementById('assignDepartment');

    // Clear existing options except the first one
    select.innerHTML = '<option value="">All Departments</option>';

    // Get unique departments
    const departments = [...new Set(allEmployees
        .map(emp => emp.department ? {id: emp.department.id, name: emp.department.departmentName} : null)
        .filter(dept => dept))];

    // Remove duplicates by department name
    const uniqueDepartments = [];
    const seenNames = new Set();

    departments.forEach(dept => {
        if (!seenNames.has(dept.name)) {
            seenNames.add(dept.name);
            uniqueDepartments.push(dept);
        }
    });

    uniqueDepartments.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept.id; // Use department ID instead of name
        option.textContent = dept.name;
        select.appendChild(option);
    });
}

function filterEmployeesByDepartment() {
    const departmentId = document.getElementById('assignDepartment').value;
    const checkboxes = document.querySelectorAll('#employeeSelection .employee-checkbox');

    checkboxes.forEach(checkbox => {
        const employeeId = checkbox.querySelector('input').value;
        const employee = allEmployees.find(emp => emp.id === employeeId);

        if (employee) {
            const show = !departmentId ||
                (employee.department && employee.department.id === departmentId) ||
                (!employee.department && departmentId === 'No Department');

            checkbox.style.display = show ? 'block' : 'none';
        }
    });
}

function populateTaskSelection() {
    const select = document.getElementById('assignTask');
    select.innerHTML = '<option value="">Select Task</option>';

    allTasks.forEach(task => {
        const option = document.createElement('option');
        option.value = task.taskId;
        option.textContent = `${task.taskName} (${task.taskId}) - Priority: ${task.priority}`;
        option.setAttribute('data-task', JSON.stringify(task));
        select.appendChild(option);
    });
}

async function loadTaskDetails() {
    const select = document.getElementById('assignTask');
    const taskDetails = document.getElementById('taskDetails');
    const taskDetailsContent = document.getElementById('taskDetailsContent');

    if (select.value) {
        try {
            // Fetch complete task details from API
            const response = await fetchWithAuth(`/api/workload/tasks/${select.value}`);
            if (response.ok) {
                const task = await response.json();

                taskDetails.style.display = 'block';
                taskDetailsContent.innerHTML = `
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                    <div><strong>Task Name:</strong></div>
                    <div>${task.taskName || 'N/A'}</div>

                    <div><strong>Task ID:</strong></div>
                    <div>${task.taskId || 'N/A'}</div>

                    <div><strong>Description:</strong></div>
                    <div>${task.description || 'N/A'}</div>

                    <div><strong>Priority:</strong></div>
                    <div><span class="priority-badge priority-${task.priority ? task.priority.toLowerCase() : ''}">
                        ${task.priority || 'N/A'}
                    </span></div>

                    <div><strong>Deadline:</strong></div>
                    <div>${task.deadline ? formatDate(task.deadline) : 'N/A'}</div>

                    <div><strong>Status:</strong></div>
                    <div><span class="status-badge status-${task.status ? task.status.toLowerCase() : ''}">
                        ${task.status ? formatStatus(task.status) : 'N/A'}
                    </span></div>

                    <div><strong>Department:</strong></div>
                    <div>${task.department ? task.department.departmentName : 'N/A'}</div>

                    <div><strong>Created Date:</strong></div>
                    <div>${task.createdDate ? formatDate(task.createdDate) : 'N/A'}</div>
                </div>
            `;
            } else {
                throw new Error('Failed to load task details');
            }
        } catch (error) {
            console.error('Error loading task details:', error);
            taskDetailsContent.innerHTML = `<p style="color: red;">Error loading task details</p>`;
            taskDetails.style.display = 'block';
        }
    } else {
        taskDetails.style.display = 'none';
    }
}

async function viewAssignmentDetails(assignmentId) {
    try {
        const response = await fetchWithAuth(`/api/assignments/${assignmentId}`);
        if (response.ok) {
            const assignment = await response.json();

            // Get department name directly from assignment
            let departmentName = 'N/A';
            if (assignment.department && assignment.department.departmentName) {
                departmentName = assignment.department.departmentName;
            }

            const detailsContent = document.getElementById('assignmentDetailsContent');
            detailsContent.innerHTML = `
            <div class="assignment-details-grid">
                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Assignment ID</div>
                    <div class="assignment-detail-value">${assignment.assignmentId || 'N/A'}</div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Employee</div>
                    <div class="assignment-detail-value">
                        ${assignment.employee ? assignment.employee.fullName : 'N/A'}
                        ${assignment.employee ? `<br><small>ID: ${assignment.employee.id}</small>` : ''}
                    </div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Task</div>
                    <div class="assignment-detail-value">
                        ${assignment.task ? assignment.task.taskName : 'N/A'}
                        ${assignment.task ? `<br><small>ID: ${assignment.task.taskId}</small>` : ''}
                    </div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Department</div>
                    <div class="assignment-detail-value" style="font-weight: 600;">${departmentName}</div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Assigned Date</div>
                    <div class="assignment-detail-value">${formatDate(assignment.assignedDate)}</div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Deadline</div>
                    <div class="assignment-detail-value">
                        ${assignment.task ? formatDate(assignment.task.deadline) : 'N/A'}
                    </div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Status</div>
                    <div class="assignment-detail-value">
                        <span class="status-badge status-${assignment.status ? assignment.status.toLowerCase() : ''}">
                            ${assignment.status ? formatStatus(assignment.status) : 'N/A'}
                        </span>
                    </div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Estimated Hours</div>
                    <div class="assignment-detail-value">${assignment.estimatedHours || 'N/A'}</div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Actual Hours</div>
                    <div class="assignment-detail-value">${assignment.actualHours || 'N/A'}</div>
                </div>

                <div class="assignment-detail-item">
                    <div class="assignment-detail-label">Completion Date</div>
                    <div class="assignment-detail-value">
                        ${assignment.completionDate ? formatDate(assignment.completionDate) : 'N/A'}
                    </div>
                </div>

                <div class="assignment-detail-item assignment-notes">
                    <div class="assignment-detail-label">Notes</div>
                    <div class="assignment-detail-value" style="min-height: 60px;">
                        ${assignment.notes || 'No notes provided'}
                    </div>
                </div>
            </div>
        `;

            document.getElementById('editAssignmentBtn').setAttribute('data-assignment-id', assignmentId);
            document.getElementById('assignmentDetailsModal').style.display = 'block';
        } else {
            throw new Error('Assignment not found');
        }
    } catch (error) {
        showNotification('Error loading assignment details: ' + error.message, 'error');
    }
}

async function editAssignment(assignmentId) {
    try {
        const response = await fetchWithAuth(`/api/assignments/${assignmentId}`);
        if (response.ok) {
            const assignment = await response.json();

            // Populate edit assignment modal
            document.getElementById('editAssignmentId').value = assignment.assignmentId;
            document.getElementById('editAssignmentStatus').value = assignment.status || 'ASSIGNED';
            document.getElementById('editAssignmentEstimatedHours').value = assignment.estimatedHours || '';
            document.getElementById('editAssignmentActualHours').value = assignment.actualHours || '';
            document.getElementById('editAssignmentCompletionDate').value = assignment.completionDate || '';
            document.getElementById('editAssignmentNotes').value = assignment.notes || '';

            // Show the edit modal
            document.getElementById('editAssignmentModal').style.display = 'block';
        } else {
            throw new Error('Failed to load assignment details');
        }
    } catch (error) {
        showNotification('Error loading assignment: ' + error.message, 'error');
    }
}

function closeAssignmentDetailsModal() {
    document.getElementById('assignmentDetailsModal').style.display = 'none';
}

function closeEditAssignmentModal() {
    document.getElementById('editAssignmentModal').style.display = 'none';
}

function openEditAssignmentModal() {
    const assignmentId = document.getElementById('editAssignmentBtn').getAttribute('data-assignment-id');
    closeAssignmentDetailsModal();
    editAssignment(assignmentId);
}

async function deleteAssignment(assignmentId) {
    if (confirm('Are you sure you want to delete this assignment?')) {
        try {
            const response = await fetchWithAuth(`/api/assignments/${assignmentId}`, {
                method: 'DELETE'
            });

            const result = await response.json();

            if (result.success) {
                showNotification('Assignment deleted successfully!', 'success');
                loadAssignments();
            } else {
                showNotification('Failed to delete assignment: ' + result.message, 'error');
            }
        } catch (error) {
            showNotification('Error deleting assignment: ' + error.message, 'error');
        }
    }
}

function updateEmployeeSelection() {
    const selectedCount = document.querySelectorAll('input[name="employeeIds"]:checked').length;
    const label = document.querySelector('label[for="employeeSelection"], .form-group label');
    if (label && label.textContent.includes('Select Employees')) {
        label.textContent = `Select Employees (${selectedCount} selected):`;
    }
}

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    console.log('Workload Assignment initialized');
    loadProductionTasks();

    // Set minimum date to today for deadline fields
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('taskDeadline').min = today;
    document.getElementById('editTaskDeadline').min = today;

    // Task form submission
    document.getElementById('taskForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(e.target);
        const taskData = Object.fromEntries(formData.entries());

        try {
            const response = await fetchWithAuth('/api/workload/tasks', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(taskData)
            });

            const result = await response.json();

            if (response.ok) {
                showAlert('Task created successfully!', 'success');
                e.target.reset();
                // Switch to task overview tab
                showTab('taskOverview');
                await loadProductionTasks();
            } else {
                showAlert(result.message || 'Failed to create task', 'error');
            }
        } catch (error) {
            console.error('Error creating task:', error);
            showAlert('Error creating task: ' + error.message, 'error');
        }
    });

    // Edit task form submission
    document.getElementById('editTaskForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(e.target);
        const taskData = Object.fromEntries(formData.entries());
        const taskId = document.getElementById('editTaskId').value;

        try {
            const response = await fetchWithAuth(`/api/workload/tasks/${taskId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(taskData)
            });

            const result = await response.json();

            if (response.ok) {
                showAlert('Task updated successfully!', 'success');
                closeEditTaskModal();
                await loadProductionTasks();
            } else {
                showAlert(result.message || 'Failed to update task', 'error');
            }
        } catch (error) {
            console.error('Error updating task:', error);
            showAlert('Error updating task: ' + error.message, 'error');
        }
    });

    // Assignment form submission
    document.getElementById('assignmentForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(this);
        const selectedEmployees = Array.from(document.querySelectorAll('input[name="employeeIds"]:checked'))
            .map(cb => cb.value);

        if (selectedEmployees.length === 0) {
            showNotification('Please select at least one employee', 'error');
            return;
        }

        const assignmentData = {
            taskId: formData.get('taskId'),
            employeeIds: selectedEmployees,
            estimatedHours: formData.get('estimatedHours') ? parseInt(formData.get('estimatedHours')) : null,
            notes: formData.get('notes')
        };

        try {
            const response = await fetchWithAuth('/api/assignments/bulk-assign', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(assignmentData)
            });

            const result = await response.json();

            if (result.success) {
                showNotification(`Task assigned successfully to ${result.assignedCount} employees!`, 'success');
                this.reset();
                document.getElementById('taskDetails').style.display = 'none';
                updateEmployeeSelection();
            } else {
                showNotification('Failed to assign task: ' + result.message, 'error');
            }
        } catch (error) {
            console.error('Error assigning task:', error);
            showNotification('Error assigning task: ' + error.message, 'error');
        }
    });

    // Edit assignment form submission
    document.getElementById('editAssignmentForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(e.target);
        const assignmentData = Object.fromEntries(formData.entries());
        const assignmentId = document.getElementById('editAssignmentId').value;

        try {
            const response = await fetchWithAuth(`/api/assignments/${assignmentId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(assignmentData)
            });

            const result = await response.json();

            if (response.ok) {
                showNotification('Assignment updated successfully!', 'success');
                closeEditAssignmentModal();
                await loadAssignments();
            } else {
                showNotification(result.message || 'Failed to update assignment', 'error');
            }
        } catch (error) {
            console.error('Error updating assignment:', error);
            showNotification('Error updating assignment: ' + error.message, 'error');
        }
    });

    // Close modals when clicking outside
    window.onclick = function(e) {
        const assignmentModal = document.getElementById('assignmentDetailsModal');
        const editAssignmentModal = document.getElementById('editAssignmentModal');
        const editTaskModal = document.getElementById('editTaskModal');

        if (e.target === assignmentModal) {
            closeAssignmentDetailsModal();
        }
        if (e.target === editAssignmentModal) {
            closeEditAssignmentModal();
        }
        if (e.target === editTaskModal) {
            closeEditTaskModal();
        }
    };

    // Close modal when clicking outside
    window.onclick = function(e) {
        const modal = document.getElementById('editTaskModal');
        if (e.target === modal) {
            closeEditTaskModal();
        }
    };

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeAssignmentDetailsModal();
            closeEditAssignmentModal();
            closeEditTaskModal();
        }
    });
});