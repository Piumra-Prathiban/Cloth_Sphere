// Production Dashboard JavaScript

// Tab switching functionality
function showTab(tabName) {
    // Hide all tab panes
    const tabPanes = document.querySelectorAll('.tab-pane');
    tabPanes.forEach(pane => pane.classList.remove('active'));

    // Remove active class from all tab buttons
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => btn.classList.remove('active'));

    // Show selected tab
    const selectedTab = document.getElementById(`${tabName}-tab`);
    if (selectedTab) {
        selectedTab.classList.add('active');
    }

    // Add active class to clicked button
    event.target.classList.add('active');

    // Load data for the selected tab
    loadTabData(tabName);
}

// Load data for specific tab
function loadTabData(tabName) {
    switch(tabName) {
        case 'orders':
            loadOrders();
            break;
        case 'schedules':
            loadSchedules();
            break;
        case 'workstations':
            loadWorkstations();
            break;
        case 'assignments':
            loadAssignments();
            break;
        case 'performance':
            loadPerformanceMetrics();
            break;
    }
}

// Load Production Orders
function loadOrders() {
    fetch('/production/api/orders')
        .then(response => response.json())
        .then(orders => {
            const tbody = document.getElementById('orders-tbody');
            if (orders.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No orders found</td></tr>';
                return;
            }

            tbody.innerHTML = orders.map(order => `
                <tr>
                    <td>${order.orderId}</td>
                    <td>${order.productName}</td>
                    <td>${order.quantity}</td>
                    <td><span class="badge ${order.priority}">${order.priority}</span></td>
                    <td><span class="badge ${order.status}">${order.status}</span></td>
                    <td>${formatDate(order.deadline)}</td>
                    <td>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${order.progressPercentage || 0}%"></div>
                        </div>
                        ${(order.progressPercentage || 0).toFixed(1)}%
                    </td>
                    <td>
                        <button class="btn-small" onclick="viewOrder('${order.orderId}')">View</button>
                        <button class="btn-small" onclick="editOrder('${order.orderId}')">Edit</button>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => console.error('Error loading orders:', error));
}

// Load Production Schedules
function loadSchedules() {
    fetch('/production/api/schedules')
        .then(response => response.json())
        .then(schedules => {
            const tbody = document.getElementById('schedules-tbody');
            if (schedules.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No schedules found</td></tr>';
                return;
            }

            tbody.innerHTML = schedules.map(schedule => `
                <tr>
                    <td>${schedule.scheduleId}</td>
                    <td>${schedule.orderId}</td>
                    <td>${schedule.workstationId}</td>
                    <td>${formatDate(schedule.scheduledDate)}</td>
                    <td>${schedule.shift}</td>
                    <td>${schedule.assignedQuantity}</td>
                    <td><span class="badge ${schedule.status}">${schedule.status}</span></td>
                    <td>
                        <button class="btn-small" onclick="viewSchedule('${schedule.scheduleId}')">View</button>
                        <button class="btn-small" onclick="editSchedule('${schedule.scheduleId}')">Edit</button>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => console.error('Error loading schedules:', error));
}

// Load Workstations
function loadWorkstations() {
    fetch('/production/api/workstations')
        .then(response => response.json())
        .then(workstations => {
            const grid = document.querySelector('.workstation-grid');
            if (workstations.length === 0) {
                grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center;">No workstations found</div>';
                return;
            }

            grid.innerHTML = workstations.map(ws => `
                <div class="workstation-card">
                    <h3>${ws.workstationName}</h3>
                    <p><strong>Type:</strong> ${ws.workstationType}</p>
                    <p><strong>Status:</strong> <span class="badge ${ws.status}">${ws.status}</span></p>
                    <p><strong>Capacity:</strong> ${ws.currentLoad}/${ws.capacity}</p>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${ws.utilizationPercentage || 0}%"></div>
                    </div>
                    <p class="utilization">${(ws.utilizationPercentage || 0).toFixed(1)}% Utilization</p>
                    <button class="btn-small" onclick="viewWorkstation('${ws.workstationId}')">View Details</button>
                </div>
            `).join('');
        })
        .catch(error => console.error('Error loading workstations:', error));
}

// Load Staff Assignments
function loadAssignments() {
    fetch('/production/api/assignments')
        .then(response => response.json())
        .then(assignments => {
            const tbody = document.getElementById('assignments-tbody');
            if (assignments.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No assignments found</td></tr>';
                return;
            }

            tbody.innerHTML = assignments.map(assignment => `
                <tr>
                    <td>${assignment.assignmentId}</td>
                    <td>${assignment.employeeName}</td>
                    <td>${assignment.workstationId}</td>
                    <td>${formatDate(assignment.assignmentDate)}</td>
                    <td>${assignment.shift}</td>
                    <td>${assignment.role}</td>
                    <td><span class="badge ${assignment.status}">${assignment.status}</span></td>
                    <td>${(assignment.performancePercentage || 0).toFixed(1)}%</td>
                </tr>
            `).join('');
        })
        .catch(error => console.error('Error loading assignments:', error));
}

// Load Performance Metrics
function loadPerformanceMetrics() {
    fetch('/production/api/metrics')
        .then(response => response.json())
        .then(metrics => {
            console.log('Performance metrics loaded:', metrics);
            // Add performance metrics visualization here
        })
        .catch(error => console.error('Error loading metrics:', error));
}

// Helper function to format dates
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
}

// Modal functions (placeholders - implement as needed)
function showAddOrderModal() {
    alert('Add Order Modal - To be implemented\n\nThis will open a form to create a new production order.');
}

function showAddScheduleModal() {
    alert('Add Schedule Modal - To be implemented\n\nThis will open a form to create a new production schedule.');
}

function showAddWorkstationModal() {
    alert('Add Workstation Modal - To be implemented\n\nThis will open a form to create a new workstation.');
}

function showAddAssignmentModal() {
    alert('Add Assignment Modal - To be implemented\n\nThis will open a form to create a new staff assignment.');
}

function showAddMetricModal() {
    alert('Add Metric Modal - To be implemented\n\nThis will open a form to record performance metrics.');
}

// View/Edit functions
function viewOrder(orderId) {
    alert(`View Order: ${orderId}\n\nThis will show detailed order information.`);
}

function editOrder(orderId) {
    alert(`Edit Order: ${orderId}\n\nThis will open an edit form for the order.`);
}

function viewSchedule(scheduleId) {
    alert(`View Schedule: ${scheduleId}\n\nThis will show detailed schedule information.`);
}

function editSchedule(scheduleId) {
    alert(`Edit Schedule: ${scheduleId}\n\nThis will open an edit form for the schedule.`);
}

function viewWorkstation(workstationId) {
    alert(`View Workstation: ${workstationId}\n\nThis will show detailed workstation information.`);
}

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Production Dashboard loaded');
    // Load initial data for the active tab
    loadOrders();
});