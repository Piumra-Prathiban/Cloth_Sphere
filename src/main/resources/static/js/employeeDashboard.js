 // Get server-side data using Thymeleaf
    const isFirstLogin = /*[[${firstLogin}]]*/ false;
    const passwordChanged = /*[[${passwordChanged}]]*/ false;
    const employeeData = /*[[${employeeData}]]*/ null;

    console.log('Server data:', { isFirstLogin, passwordChanged, employeeData });

    // Section Navigation
    function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });

    // Remove active class from all nav links
    document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.remove('active');
});

    // Show selected section
    document.getElementById(sectionId).classList.add('active');

    // Add active class to clicked nav link
    event.target.classList.add('active');

    // If first login and not profile section, show restriction message
    if (isFirstLogin && sectionId !== 'profile') {
    showRestrictedAccess(sectionId);
}
}

    // Show restricted access message
    function showRestrictedAccess(sectionId) {
    const contentElement = document.getElementById(sectionId + '-content');
    const restrictedElement = document.getElementById(sectionId + '-restricted');

    if (contentElement) contentElement.style.display = 'none';
    if (restrictedElement) restrictedElement.style.display = 'block';
}

    // Hide restricted access message
    function hideRestrictedAccess(sectionId) {
    const contentElement = document.getElementById(sectionId + '-content');
    const restrictedElement = document.getElementById(sectionId + '-restricted');

    if (contentElement) contentElement.style.display = 'block';
    if (restrictedElement) restrictedElement.style.display = 'none';
}

    // Logout Function
    function logout() {
    if (confirm('Are you sure you want to logout?')) {
    window.location.href = '/systemUserLogin';
}
}

    // Update Password Function
    function updatePassword() {
    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (!currentPassword || !newPassword || !confirmPassword) {
    alert('Please fill in all password fields');
    return;
}

    if (newPassword.length < 6) {
    alert('New password must be at least 6 characters long');
    return;
}

    if (newPassword !== confirmPassword) {
    alert('New password and confirmation do not match');
    return;
}

    // Show loading state
    const updateBtn = document.querySelector('.btn-success');
    const originalText = updateBtn.innerHTML;
    updateBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';
    updateBtn.disabled = true;

    // Send password update request
    fetch('/updateEmployeePassword', {
    method: 'POST',
    headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
},
    body: `currentPassword=${encodeURIComponent(currentPassword)}&newPassword=${encodeURIComponent(newPassword)}&confirmPassword=${encodeURIComponent(confirmPassword)}`
})
    .then(response => response.json())
    .then(data => {
    if (data.success) {
    // Show success message
    document.getElementById('password-update-success').style.display = 'block';

    // Hide first login alerts and show normal alert
    document.getElementById('first-login-alert').style.display = 'none';
    document.getElementById('normal-password-alert').style.display = 'block';
    const helpElement = document.getElementById('current-password-help');
    if (helpElement) helpElement.style.display = 'none';

    // Clear the password fields
    document.getElementById('current-password').value = '';
    document.getElementById('new-password').value = '';
    document.getElementById('confirm-password').value = '';

    // Enable all sections if this was first login
    if (isFirstLogin) {
    ['attendance', 'leave', 'tasks', 'paysheet'].forEach(section => {
    hideRestrictedAccess(section);
});
}

    // Hide first login modal if it's open
    const firstLoginModal = document.getElementById('firstLoginModal');
    if (firstLoginModal) firstLoginModal.style.display = 'none';

    // Show global success message
    const passwordChangedAlert = document.getElementById('passwordChangedAlert');
    if (passwordChangedAlert) {
    passwordChangedAlert.style.display = 'block';
    setTimeout(() => {
    passwordChangedAlert.style.display = 'none';
}, 5000);
}

    // Redirect to remove URL parameters and show success
    setTimeout(() => {
    window.location.href = '/employeeDashboard?passwordChanged=true';
}, 1500);

} else {
    alert('Error: ' + data.message);
}
})
    .catch(error => {
    console.error('Error:', error);
    alert('Error updating password. Please try again.');
})
    .finally(() => {
    // Restore button state
    updateBtn.innerHTML = originalText;
    updateBtn.disabled = false;
});
}

    // Cancel Password Change
    function cancelPasswordChange() {
    document.getElementById('current-password').value = '';
    document.getElementById('new-password').value = '';
    document.getElementById('confirm-password').value = '';
    document.getElementById('password-update-success').style.display = 'none';
}

    // Close First Login Modal
    function closeFirstLoginModal() {
    document.getElementById('firstLoginModal').style.display = 'none';
    showSection('profile');
}

    // Initialize when page loads
    window.onload = function() {
    console.log('Employee Dashboard initializing...');
    console.log('First login status:', isFirstLogin);
    console.log('Password changed:', passwordChanged);

    // Show first login modal if needed
    if (isFirstLogin) {
    document.getElementById('firstLoginModal').style.display = 'flex';
}

    // Show password changed success message if needed
    if (passwordChanged) {
    const passwordChangedAlert = document.getElementById('passwordChangedAlert');
    if (passwordChangedAlert) {
    passwordChangedAlert.style.display = 'block';
    setTimeout(() => {
    passwordChangedAlert.style.display = 'none';
}, 5000);
}

    // Clean URL to remove parameter
    if (window.history.replaceState) {
    window.history.replaceState(null, null, '/employeeDashboard');
}
}
};

 // Task Management
 let currentTasks = [];
 let currentAssignmentId = null;

 // Load employee tasks
 function loadEmployeeTasks() {
     const loadingElement = document.getElementById('tasks-loading');
     const tableBody = document.getElementById('tasks-table-body');
     const noTasksMessage = document.getElementById('no-tasks-message');

     if (loadingElement) loadingElement.style.display = 'block';
     if (tableBody) tableBody.innerHTML = '';
     if (noTasksMessage) noTasksMessage.style.display = 'none';

     fetch('/api/employee/tasks', {
         method: 'GET',
         headers: {
             'Content-Type': 'application/json',
         }
     })
         .then(response => {
             if (!response.ok) {
                 throw new Error('Failed to load tasks');
             }
             return response.json();
         })
         .then(tasks => {
             currentTasks = tasks;
             displayTasks(tasks);
             updateTaskStatistics(tasks);
         })
         .catch(error => {
             console.error('Error loading tasks:', error);
             showMessage('Error loading tasks: ' + error.message, 'error');
         })
         .finally(() => {
             if (loadingElement) loadingElement.style.display = 'none';
         });
 }

 // Display tasks in table
 // Display tasks in table - Enhanced version
 function displayTasks(tasks) {
     const tableBody = document.getElementById('tasks-table-body');
     const noTasksMessage = document.getElementById('no-tasks-message');

     if (!tableBody) return;

     if (!tasks || tasks.length === 0) {
         tableBody.innerHTML = '';
         if (noTasksMessage) noTasksMessage.style.display = 'block';
         return;
     }

     if (noTasksMessage) noTasksMessage.style.display = 'none';

     const tasksHtml = tasks.map(task => {
         const isOverdue = new Date(task.deadline) < new Date() && task.status !== 'COMPLETED';
         const rowClass = isOverdue ? 'overdue-task' : '';

         // Format completion date and show actual hours if completed
         const completionInfo = task.status === 'COMPLETED' && task.completionDate ?
             `<br><small class="text-success">Completed: ${formatDate(task.completionDate)}${task.actualHours ? ` (${task.actualHours}h)` : ''}</small>` :
             '';

         return `
            <tr class="${rowClass}">
                <td>${task.assignmentId || 'N/A'}</td>
                <td>
                    <strong>${task.taskName}</strong>
                    ${task.description ? `<br><small>${task.description}</small>` : ''}
                </td>
                <td>${task.departmentName || 'N/A'}</td>
                <td>${formatDate(task.assignedDate)}</td>
                <td class="${isOverdue ? 'overdue-deadline' : ''}">
                    ${formatDate(task.deadline)}
                    ${isOverdue ? '<br><small class="overdue-deadline">Overdue</small>' : ''}
                </td>
                <td>
                    ${task.estimatedHours || 'N/A'} hrs
                    ${task.actualHours ? `<br><small class="text-success">Actual: ${task.actualHours}h</small>` : ''}
                </td>
                <td>
                    <span class="status-badge status-${task.status.toLowerCase().replace('_', '-')}">
                        ${getStatusLabel(task.status)}
                    </span>
                    ${completionInfo}
                </td>
                <td>
                    <div class="task-actions">
                        ${task.status !== 'COMPLETED' ? `
                            <button class="btn btn-primary btn-sm" onclick="openStatusModal('${task.assignmentId}')">
                                <i class="fas fa-edit"></i> Update
                            </button>
                        ` : ''}
                        <button class="btn btn-info btn-sm" onclick="viewTaskDetails('${task.assignmentId}')">
                            <i class="fas fa-eye"></i> View
                        </button>
                    </div>
                </td>
            </tr>
        `;
     }).join('');

     tableBody.innerHTML = tasksHtml;
 }

 // Update task statistics
 function updateTaskStatistics(tasks) {
     if (!tasks) return;

     const total = tasks.length;
     const assigned = tasks.filter(t => t.status === 'ASSIGNED').length;
     const inProgress = tasks.filter(t => t.status === 'IN_PROGRESS').length;
     const completed = tasks.filter(t => t.status === 'COMPLETED').length;

     document.getElementById('total-tasks').textContent = total;
     document.getElementById('assigned-tasks').textContent = assigned;
     document.getElementById('inprogress-tasks').textContent = inProgress;
     document.getElementById('completed-tasks').textContent = completed;
 }

 // Filter tasks by status
 function filterTasks() {
     const statusFilter = document.getElementById('status-filter').value;
     const searchTerm = document.getElementById('task-search').value.toLowerCase();

     let filteredTasks = currentTasks;

     if (statusFilter !== 'all') {
         filteredTasks = filteredTasks.filter(task => task.status === statusFilter);
     }

     if (searchTerm) {
         filteredTasks = filteredTasks.filter(task =>
             task.taskName.toLowerCase().includes(searchTerm) ||
             (task.description && task.description.toLowerCase().includes(searchTerm)) ||
             (task.departmentName && task.departmentName.toLowerCase().includes(searchTerm))
         );
     }

     displayTasks(filteredTasks);
 }

 // Search tasks
 function searchTasks() {
     filterTasks();
 }

 // Open status update modal
 // Open status update modal - Enhanced version with actual hours input
 function openStatusModal(assignmentId) {
     currentAssignmentId = assignmentId;
     const task = currentTasks.find(t => t.assignmentId === assignmentId);

     if (!task) return;

     const modalHtml = `
        <div class="status-modal" id="status-modal">
            <div class="status-modal-content">
                <div class="status-modal-header">
                    <h4>Update Task Status</h4>
                </div>
                <div class="status-modal-body">
                    <p><strong>Task:</strong> ${task.taskName}</p>
                    <p><strong>Current Status:</strong> ${getStatusLabel(task.status)}</p>
                    
                    <h5>Select New Status:</h5>
                    
                    ${task.status === 'ASSIGNED' ? `
                        <div class="status-option" onclick="selectStatus('IN_PROGRESS')">
                            <h5>In Progress</h5>
                            <p>Start working on this task</p>
                        </div>
                    ` : ''}
                    
                    ${task.status === 'IN_PROGRESS' ? `
                        <div class="status-option" onclick="selectStatus('COMPLETED')">
                            <h5>Completed</h5>
                            <p>Mark this task as finished</p>
                        </div>
                    ` : ''}
                    
                    <!-- Actual Hours Input (only shown when COMPLETED is selected) -->
                    <div id="actual-hours-section" style="margin-top: 15px; display: none;">
                        <div class="form-group">
                            <label for="actual-hours-input"><strong>Actual Hours Worked:</strong></label>
                            <input type="number" id="actual-hours-input" class="form-control" 
                                   placeholder="Enter hours worked" min="1" max="24" 
                                   value="${task.estimatedHours || 8}">
                            <small class="form-text">Please enter the actual hours you spent on this task</small>
                        </div>
                    </div>
                    
                    <div id="selected-status" style="margin-top: 15px; display: none;">
                        <strong>Selected: </strong><span id="selected-status-label"></span>
                    </div>
                </div>
                <div class="status-modal-footer">
                    <button class="btn btn-secondary" onclick="closeStatusModal()">Cancel</button>
                    <button class="btn btn-primary" id="update-status-btn" onclick="updateTaskStatus()" disabled>
                        Update Status
                    </button>
                </div>
            </div>
        </div>
    `;

     // Remove existing modal if any
     const existingModal = document.getElementById('status-modal');
     if (existingModal) {
         existingModal.remove();
     }

     document.body.insertAdjacentHTML('beforeend', modalHtml);
 }


 // Select status in modal
 let selectedStatus = null;

 function selectStatus(status) {
     selectedStatus = status;

     // Update UI
     document.querySelectorAll('.status-option').forEach(option => {
         option.classList.remove('selected');
     });
     event.target.classList.add('selected');

     const selectedStatusDiv = document.getElementById('selected-status');
     const selectedStatusLabel = document.getElementById('selected-status-label');
     const updateBtn = document.getElementById('update-status-btn');

     selectedStatusLabel.textContent = getStatusLabel(status);
     selectedStatusDiv.style.display = 'block';
     updateBtn.disabled = false;
 }

 // Close status modal
 function closeStatusModal() {
     const modal = document.getElementById('status-modal');
     if (modal) {
         modal.remove();
     }
     selectedStatus = null;
 }

 // Update task status - Enhanced version
 // Update task status - Enhanced version with completion date and actual hours
 // Update task status - Enhanced version with actual hours input
 function updateTaskStatus() {
     if (!currentAssignmentId || !selectedStatus) {
         showMessage('Please select a status', 'error');
         return;
     }

     const updateBtn = document.getElementById('update-status-btn');
     updateBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';
     updateBtn.disabled = true;

     // Get actual hours if COMPLETED status
     let actualHours = null;
     if (selectedStatus === 'COMPLETED') {
         const actualHoursInput = document.getElementById('actual-hours-input');
         actualHours = parseInt(actualHoursInput.value);

         if (!actualHours || actualHours < 1) {
             showMessage('Please enter valid actual hours (minimum 1 hour)', 'error');
             updateBtn.innerHTML = 'Update Status';
             updateBtn.disabled = false;
             return;
         }
     }

     // Prepare request data
     const requestData = {
         status: selectedStatus
     };

     // Add actual hours only for COMPLETED status
     if (selectedStatus === 'COMPLETED' && actualHours) {
         requestData.actualHours = actualHours;
     }

     // Use the employee-specific endpoint
     fetch(`/api/assignments/employee/${currentAssignmentId}/status`, {
         method: 'PUT',
         headers: {
             'Content-Type': 'application/json',
         },
         body: JSON.stringify(requestData)
     })
         .then(response => {
             if (!response.ok) {
                 throw new Error('Failed to update status');
             }
             return response.json();
         })
         .then(data => {
             if (data.success) {
                 let message = 'Task status updated successfully!';

                 // Add completion information if status is COMPLETED
                 if (selectedStatus === 'COMPLETED') {
                     message += ` Completed on ${new Date(data.completionDate).toLocaleDateString()}`;
                     if (data.actualHours) {
                         message += ` with ${data.actualHours} hours worked`;
                     }
                 }

                 // Add task progress information if available
                 if (data.taskProgress) {
                     const progress = data.taskProgress;
                     message += ` Task progress: ${progress.completedAssignments}/${progress.totalAssignments} completed`;

                     if (data.taskStatus) {
                         message += ` (Overall task: ${getStatusLabel(data.taskStatus)})`;
                     }
                 }

                 showMessage(message, 'success');
                 closeStatusModal();
                 loadEmployeeTasks(); // Reload tasks to show updated completion date and hours
             } else {
                 throw new Error(data.message || 'Failed to update status');
             }
         })
         .catch(error => {
             console.error('Error updating status:', error);
             showMessage('Error updating status: ' + error.message, 'error');
             updateBtn.innerHTML = 'Update Status';
             updateBtn.disabled = false;
         });
 }

 // View task details
 // View task details - Enhanced version
 function viewTaskDetails(assignmentId) {
     const task = currentTasks.find(t => t.assignmentId === assignmentId);
     if (!task) return;

     const detailsHtml = `
        <div class="status-modal" id="task-details-modal">
            <div class="status-modal-content" style="max-width: 600px;">
                <div class="status-modal-header">
                    <h4>Task Details</h4>
                </div>
                <div class="status-modal-body">
                    <div class="profile-info">
                        <div class="info-group">
                            <label>Task Name</label>
                            <div class="value">${task.taskName}</div>
                        </div>
                        <div class="info-group">
                            <label>Description</label>
                            <div class="value">${task.description || 'No description'}</div>
                        </div>
                        <div class="info-group">
                            <label>Department</label>
                            <div class="value">${task.departmentName || 'N/A'}</div>
                        </div>
                        <div class="info-group">
                            <label>Assignment ID</label>
                            <div class="value">${task.assignmentId || 'N/A'}</div>
                        </div>
                        <div class="info-group">
                            <label>Assigned Date</label>
                            <div class="value">${formatDate(task.assignedDate)}</div>
                        </div>
                        <div class="info-group">
                            <label>Deadline</label>
                            <div class="value ${new Date(task.deadline) < new Date() && task.status !== 'COMPLETED' ? 'overdue-deadline' : ''}">
                                ${formatDate(task.deadline)}
                                ${new Date(task.deadline) < new Date() && task.status !== 'COMPLETED' ? ' (Overdue)' : ''}
                            </div>
                        </div>
                        <div class="info-group">
                            <label>Estimated Hours</label>
                            <div class="value">${task.estimatedHours || 'N/A'} hours</div>
                        </div>
                        ${task.actualHours ? `
                        <div class="info-group">
                            <label>Actual Hours</label>
                            <div class="value text-success">${task.actualHours} hours</div>
                        </div>
                        ` : ''}
                        ${task.completionDate ? `
                        <div class="info-group">
                            <label>Completion Date</label>
                            <div class="value text-success">${formatDate(task.completionDate)}</div>
                        </div>
                        ` : ''}
                        <div class="info-group">
                            <label>Current Status</label>
                            <div class="value">
                                <span class="status-badge status-${task.status.toLowerCase().replace('_', '-')}">
                                    ${getStatusLabel(task.status)}
                                </span>
                            </div>
                        </div>
                        ${task.notes ? `
                        <div class="info-group">
                            <label>Notes</label>
                            <div class="value">${task.notes}</div>
                        </div>
                        ` : ''}
                    </div>
                </div>
                <div class="status-modal-footer">
                    <button class="btn btn-secondary" onclick="closeTaskDetails()">Close</button>
                    ${task.status !== 'COMPLETED' ? `
                    <button class="btn btn-primary" onclick="closeTaskDetails(); openStatusModal('${task.assignmentId}')">
                        Update Status
                    </button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;

     // Remove existing modal if any
     const existingModal = document.getElementById('task-details-modal');
     if (existingModal) {
         existingModal.remove();
     }

     document.body.insertAdjacentHTML('beforeend', detailsHtml);
 }

 // Close task details modal
 function closeTaskDetails() {
     const modal = document.getElementById('task-details-modal');
     if (modal) {
         modal.remove();
     }
 }

 // Utility functions
 function formatDate(dateString) {
     if (!dateString) return 'N/A';
     const date = new Date(dateString);
     return date.toLocaleDateString('en-US', {
         year: 'numeric',
         month: 'short',
         day: 'numeric'
     });
 }

 function getStatusLabel(status) {
     const statusLabels = {
         'ASSIGNED': 'Assigned',
         'IN_PROGRESS': 'In Progress',
         'COMPLETED': 'Completed',
         'PENDING': 'Pending'
     };
     return statusLabels[status] || status;
 }

 function showMessage(message, type) {
     // Create toast notification
     const toast = document.createElement('div');
     toast.className = `alert alert-${type === 'error' ? 'error' : 'success'}`;
     toast.style.position = 'fixed';
     toast.style.top = '20px';
     toast.style.right = '20px';
     toast.style.zIndex = '1001';
     toast.style.minWidth = '300px';
     toast.innerHTML = `
        <i class="fas ${type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle'}"></i>
        ${message}
    `;

     document.body.appendChild(toast);

     setTimeout(() => {
         toast.remove();
     }, 5000);
 }

 // Initialize tasks when tasks section is shown
 const originalShowSection = showSection;
 showSection = function(sectionId) {
     originalShowSection(sectionId);

     if (sectionId === 'tasks' && !isFirstLogin) {
         loadEmployeeTasks();
     }
 };