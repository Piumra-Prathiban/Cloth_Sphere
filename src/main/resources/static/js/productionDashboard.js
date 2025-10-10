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

// Modal functions
function showAddOrderModal() {
    console.log('🎯 showAddOrderModal() called');
    const modal = document.getElementById('addOrderModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'block';
        console.log('✅ Order modal opened');
        // Set today's date as default
        document.getElementById('orderDate').value = new Date().toISOString().split('T')[0];
    } else {
        console.error('❌ addOrderModal element not found!');
    }
}

function showAddScheduleModal() {
    console.log('🎯 showAddScheduleModal() called');
    const modal = document.getElementById('addScheduleModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'block';
        console.log('✅ Schedule modal opened');
        document.getElementById('scheduledDate').value = new Date().toISOString().split('T')[0];
    } else {
        console.error('❌ addScheduleModal element not found!');
    }
}

function showAddWorkstationModal() {
    console.log('🎯 showAddWorkstationModal() called');
    const modal = document.getElementById('addWorkstationModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'block';
        console.log('✅ Workstation modal opened');
    } else {
        console.error('❌ addWorkstationModal element not found!');
    }
}

function showAddAssignmentModal() {
    console.log('🎯 showAddAssignmentModal() called');
    const modal = document.getElementById('addAssignmentModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'block';
        console.log('✅ Assignment modal opened');
        document.getElementById('assignmentDate').value = new Date().toISOString().split('T')[0];
    } else {
        console.error('❌ addAssignmentModal element not found!');
    }
}

function showAddMetricModal() {
    console.log('🎯 showAddMetricModal() called');
    alert('Performance Metrics feature coming soon!');
}

function closeModal(modalId) {
    console.log('🎯 closeModal() called for:', modalId);
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
        modal.style.display = 'none';
        console.log('✅ Modal closed:', modalId);
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}

// Form submission functions
function submitOrder(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const orderData = {
        productName: formData.get('productName'),
        productType: formData.get('productType'),
        quantity: parseInt(formData.get('quantity')),
        priority: formData.get('priority'),
        status: 'PENDING',
        orderDate: formData.get('orderDate'),
        deadline: formData.get('deadline'),
        customerName: formData.get('customerName'),
        customerId: formData.get('customerId'),
        notes: formData.get('notes')
    };

    fetch('/production/api/orders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Order created successfully!');
            closeModal('addOrderModal');
            event.target.reset();
            loadOrders(); // Reload the orders table
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to create order. Please try again.');
    });
}

function submitSchedule(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const scheduleData = {
        orderId: formData.get('orderId'),
        workstationId: formData.get('workstationId'),
        scheduledDate: formData.get('scheduledDate'),
        shift: formData.get('shift'),
        assignedQuantity: parseInt(formData.get('assignedQuantity')),
        status: 'SCHEDULED'
    };

    fetch('/production/api/schedules', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(scheduleData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Schedule created successfully!');
            closeModal('addScheduleModal');
            event.target.reset();
            loadSchedules();
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to create schedule. Please try again.');
    });
}

function submitWorkstation(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const workstationData = {
        workstationName: formData.get('workstationName'),
        workstationType: formData.get('workstationType'),
        capacity: parseInt(formData.get('capacity')),
        status: 'ACTIVE',
        location: formData.get('location'),
        supervisorName: formData.get('supervisorName'),
        supervisorId: formData.get('supervisorId')
    };

    fetch('/production/api/workstations', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(workstationData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Workstation created successfully!');
            closeModal('addWorkstationModal');
            event.target.reset();
            loadWorkstations();
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to create workstation. Please try again.');
    });
}

function submitAssignment(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const assignmentData = {
        scheduleId: formData.get('scheduleId'),
        employeeId: formData.get('employeeId'),
        employeeName: formData.get('employeeName'),
        workstationId: formData.get('workstationId'),
        assignmentDate: formData.get('assignmentDate'),
        shift: formData.get('shift'),
        role: formData.get('role'),
        assignedQuantity: formData.get('assignedQuantity') ? parseInt(formData.get('assignedQuantity')) : null,
        status: 'ASSIGNED'
    };

    fetch('/production/api/assignments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(assignmentData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Assignment created successfully!');
            closeModal('addAssignmentModal');
            event.target.reset();
            loadAssignments();
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to create assignment. Please try again.');
    });
}

// View/Edit functions
function viewOrder(orderId) {
    fetch(`/production/api/orders/${orderId}`)
        .then(response => response.json())
        .then(order => {
            const details = `Order Details:

Order ID: ${order.orderId}
Product: ${order.productName} (${order.productType || 'N/A'})
Quantity: ${order.quantity}
Completed: ${order.completedQuantity || 0}
Priority: ${order.priority}
Status: ${order.status}
Customer: ${order.customerName || 'N/A'}
Order Date: ${order.orderDate}
Deadline: ${order.deadline}
Progress: ${order.progressPercentage?.toFixed(1) || 0}%
Notes: ${order.notes || 'None'}`;

            alert(details);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to load order details.');
        });
}

function editOrder(orderId) {
    alert(`Edit Order: ${orderId}\n\nEdit functionality coming soon!`);
}

function viewSchedule(scheduleId) {
    fetch(`/production/api/schedules/${scheduleId}`)
        .then(response => response.json())
        .then(schedule => {
            const details = `Schedule Details:

Schedule ID: ${schedule.scheduleId}
Order ID: ${schedule.orderId}
Workstation: ${schedule.workstationId}
Date: ${schedule.scheduledDate}
Shift: ${schedule.shift}
Assigned Quantity: ${schedule.assignedQuantity}
Status: ${schedule.status}`;

            alert(details);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to load schedule details.');
        });
}

function editSchedule(scheduleId) {
    alert(`Edit Schedule: ${scheduleId}\n\nEdit functionality coming soon!`);
}

function viewWorkstation(workstationId) {
    // Fetch from API or find in current data
    fetch(`/production/api/workstations`)
        .then(response => response.json())
        .then(workstations => {
            const ws = workstations.find(w => w.workstationId === workstationId);
            if (ws) {
                const details = `Workstation Details:

ID: ${ws.workstationId}
Name: ${ws.workstationName}
Type: ${ws.workstationType}
Status: ${ws.status}
Capacity: ${ws.capacity} workers
Current Load: ${ws.currentLoad || 0} workers
Utilization: ${ws.utilizationPercentage?.toFixed(1) || 0}%
Location: ${ws.location || 'N/A'}
Supervisor: ${ws.supervisorName || 'N/A'}`;

                alert(details);
            } else {
                alert('Workstation not found!');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to load workstation details.');
        });
}

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Production Dashboard JavaScript loaded successfully');
    console.log('✅ Modal functions available:', {
        showAddOrderModal: typeof showAddOrderModal,
        showAddScheduleModal: typeof showAddScheduleModal,
        showAddWorkstationModal: typeof showAddWorkstationModal,
        showAddAssignmentModal: typeof showAddAssignmentModal
    });

    // Load initial data for the active tab
    loadOrders();

    // Test modal availability
    const modals = ['addOrderModal', 'addScheduleModal', 'addWorkstationModal', 'addAssignmentModal'];
    modals.forEach(modalId => {
        const modal = document.getElementById(modalId);
        if (modal) {
            console.log(`✅ Modal found: ${modalId}`);
        } else {
            console.error(`❌ Modal NOT found: ${modalId}`);
        }
    });
});