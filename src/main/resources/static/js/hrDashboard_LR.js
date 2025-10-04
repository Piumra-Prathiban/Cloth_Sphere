// hrDashboard_LR.js - Complete Leave Request Management System

// Leave Management Variables
let allLeaves = [];
let filteredLeaves = [];
let currentRejectLeaveId = null;
let currentApproveLeaveId = null;

// Load all leave requests
async function loadLeaveRequests() {
    try {
        console.log('=== LOADING LEAVE REQUESTS ===');

        // Show loading indicator
        const tbody = document.querySelector('#leaveTable tbody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="10" style="text-align: center;"><i class="fas fa-spinner fa-spin"></i> Loading leave requests...</td></tr>';
        }

        const response = await fetchWithAuth('/hr/leave/requests');
        console.log('Response status:', response.status);

        if (response.ok) {
            const result = await response.json();
            console.log('Response data:', result);

            if (result.success && result.leaves) {
                allLeaves = result.leaves;

                // Clean up status data
                allLeaves.forEach(leave => {
                    if (leave.status) {
                        leave.status = leave.status.replace(/[^a-zA-Z]/g, '').toUpperCase();
                        if (!['PENDING', 'APPROVED', 'REJECTED'].includes(leave.status)) {
                            console.warn(`Invalid status: ${leave.status}, defaulting to PENDING`);
                            leave.status = 'PENDING';
                        }
                    } else {
                        leave.status = 'PENDING';
                    }
                    console.log(`Leave ${leave.leaveId}: Status = ${leave.status}`);
                });

                console.log(`Successfully loaded ${allLeaves.length} leave requests`);
                filterLeaves();
                showAlert(`Loaded ${allLeaves.length} leave requests`, 'success');
            } else {
                console.error('Invalid response format:', result);
                allLeaves = [];
                refreshLeaveTable();
                showAlert('No leave requests found', 'info');
            }
        } else {
            console.error('Failed to load. Status:', response.status);
            allLeaves = [];
            refreshLeaveTable();
            showAlert('Failed to load leave requests', 'error');
        }
    } catch (error) {
        console.error('Error loading leave requests:', error);
        allLeaves = [];
        refreshLeaveTable();
        showAlert('Error: ' + error.message, 'error');
    }
}

// Filter leaves
function filterLeaves() {
    const statusFilter = document.getElementById('statusFilter')?.value || 'ALL';
    const employeeFilter = document.getElementById('employeeFilter')?.value.toLowerCase() || '';

    filteredLeaves = allLeaves.filter(leave => {
        const statusMatch = statusFilter === 'ALL' || leave.status === statusFilter;
        const employeeMatch = !employeeFilter ||
            (leave.employee &&
                (leave.employee.id.toLowerCase().includes(employeeFilter) ||
                    (leave.employee.fullName && leave.employee.fullName.toLowerCase().includes(employeeFilter))));
        return statusMatch && employeeMatch;
    });

    console.log(`Filtered: ${filteredLeaves.length} leaves`);
    refreshLeaveTable();
}

// Refresh table
function refreshLeaveTable() {
    const tbody = document.querySelector('#leaveTable tbody');
    if (!tbody) {
        console.error('Leave table tbody not found!');
        return;
    }

    tbody.innerHTML = '';

    if (filteredLeaves.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 20px; color: #6c757d;"><i class="fas fa-inbox"></i> No leave requests found</td></tr>';
        return;
    }

    filteredLeaves.forEach(leave => {
        const cleanStatus = leave.status || 'PENDING';
        const statusClass = `status-${cleanStatus.toLowerCase()}`;

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${leave.leaveId || 'N/A'}</td>
            <td>${leave.employee ? leave.employee.id : 'N/A'}</td>
            <td>${leave.employee ? leave.employee.fullName : 'N/A'}</td>
            <td title="${leave.reason || 'N/A'}">${truncateText(leave.reason || 'N/A', 30)}</td>
            <td>${leave.startDate || 'N/A'}</td>
            <td>${leave.endDate || 'N/A'}</td>
            <td style="text-align: center;">${calculateTotalDays(leave.startDate, leave.endDate)}</td>
            <td style="text-align: center;">
                <span class="status-badge ${statusClass}">${cleanStatus}</span>
            </td>
            <td>${formatDate(leave.requestDate)}</td>
            <td>
                <div class="action-buttons">
                    ${cleanStatus === 'PENDING' ? `
                        <button class="btn btn-success btn-sm" onclick="approveLeave('${leave.leaveId}')" title="Approve">
                            <i class="fas fa-check"></i> Approve
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="rejectLeave('${leave.leaveId}')" title="Reject">
                            <i class="fas fa-times"></i> Reject
                        </button>
                    ` : `
                        <button class="btn btn-info btn-sm" onclick="viewLeaveDetails('${leave.leaveId}')" title="View">
                            <i class="fas fa-eye"></i> View
                        </button>
                    `}
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Helper functions
function truncateText(text, maxLength) {
    if (!text) return 'N/A';
    return text.length <= maxLength ? text : text.substring(0, maxLength) + '...';
}

function calculateTotalDays(startDate, endDate) {
    if (!startDate || !endDate) return 'N/A';
    try {
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffDays = Math.ceil(Math.abs(end - start) / (1000 * 60 * 60 * 24)) + 1;
        return diffDays;
    } catch (error) {
        return 'N/A';
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    } catch (error) {
        return 'N/A';
    }
}

// Approve leave
async function approveLeave(leaveId) {
    console.log('Approving:', leaveId);
    currentApproveLeaveId = leaveId;

    const leave = allLeaves.find(l => l.leaveId === leaveId);
    if (leave) {
        document.getElementById('approveLeaveDetails').innerHTML = `
            <div style="font-size: 14px;">
                <strong>Leave Request Details:</strong><br>
                <div style="margin-top: 8px;">
                    <strong>Employee:</strong> ${leave.employee ? leave.employee.fullName : 'N/A'} (${leave.employee ? leave.employee.id : 'N/A'})<br>
                    <strong>Period:</strong> ${leave.startDate} to ${leave.endDate}<br>
                    <strong>Total Days:</strong> ${calculateTotalDays(leave.startDate, leave.endDate)}<br>
                    <strong>Reason:</strong> ${leave.reason || 'N/A'}
                </div>
            </div>
        `;
        document.getElementById('approveLeaveModal').style.display = 'block';
    }
}

// Reject leave
async function rejectLeave(leaveId) {
    console.log('Rejecting:', leaveId);
    currentRejectLeaveId = leaveId;

    const leave = allLeaves.find(l => l.leaveId === leaveId);
    if (leave) {
        document.getElementById('rejectLeaveDetails').innerHTML = `
            <div style="font-size: 14px;">
                <strong>Leave Request Details:</strong><br>
                <div style="margin-top: 8px;">
                    <strong>Employee:</strong> ${leave.employee ? leave.employee.fullName : 'N/A'} (${leave.employee ? leave.employee.id : 'N/A'})<br>
                    <strong>Period:</strong> ${leave.startDate} to ${leave.endDate}<br>
                    <strong>Total Days:</strong> ${calculateTotalDays(leave.startDate, leave.endDate)}<br>
                    <strong>Reason:</strong> ${leave.reason || 'N/A'}
                </div>
            </div>
        `;
        document.getElementById('rejectComments').value = '';
        document.getElementById('rejectLeaveModal').style.display = 'block';
    }
}

// View details
async function viewLeaveDetails(leaveId) {
    const leave = allLeaves.find(l => l.leaveId === leaveId);
    if (leave) {
        const cleanStatus = leave.status || 'PENDING';
        document.getElementById('leaveDetailsContent').innerHTML = `
            <div class="detail-section">
                <h4 style="margin-bottom: 15px; color: #333; border-bottom: 2px solid #007bff; padding-bottom: 5px;">Employee Information</h4>
                <div class="detail-row">
                    <span class="detail-label">Employee ID:</span>
                    <span class="detail-value">${leave.employee ? leave.employee.id : 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Employee Name:</span>
                    <span class="detail-value">${leave.employee ? leave.employee.fullName : 'N/A'}</span>
                </div>
            </div>
            <div class="detail-section">
                <h4 style="margin-bottom: 15px; color: #333; border-bottom: 2px solid #007bff; padding-bottom: 5px;">Leave Details</h4>
                <div class="detail-row">
                    <span class="detail-label">Leave ID:</span>
                    <span class="detail-value">#${leave.leaveId || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Reason:</span>
                    <span class="detail-value" style="background: #f8f9fa; padding: 8px; border-radius: 4px;">${leave.reason || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Period:</span>
                    <span class="detail-value">${leave.startDate} to ${leave.endDate}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Total Days:</span>
                    <span class="detail-value"><strong>${calculateTotalDays(leave.startDate, leave.endDate)}</strong></span>
                </div>
            </div>
            <div class="detail-section">
                <h4 style="margin-bottom: 15px; color: #333; border-bottom: 2px solid #007bff; padding-bottom: 5px;">Status</h4>
                <div class="detail-row">
                    <span class="detail-label">Status:</span>
                    <span class="detail-value">
                        <span class="status-badge status-${cleanStatus.toLowerCase()} status-badge-large">${cleanStatus}</span>
                    </span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Request Date:</span>
                    <span class="detail-value">${formatDate(leave.requestDate)}</span>
                </div>
                ${leave.comments ? `
                <div class="detail-row">
                    <span class="detail-label">HR Comments:</span>
                    <span class="detail-value" style="background: #e9ecef; padding: 10px; border-radius: 4px; border-left: 4px solid #dc3545; font-style: italic;">"${leave.comments}"</span>
                </div>
                ` : ''}
            </div>
        `;
        document.getElementById('leaveDetailsModal').style.display = 'block';
    }
}

// Update status
async function updateLeaveStatus(leaveId, status, comments = '') {
    try {
        console.log(`Updating ${leaveId} to ${status}`);

        const url = `/hr/leave/${leaveId}/status?status=${status}` +
            (comments ? `&comments=${encodeURIComponent(comments)}` : '');

        const response = await fetchWithAuth(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });

        const result = await response.json();

        if (result.success) {
            const msg = status === 'APPROVED' ? 'approved' :
                status === 'REJECTED' ? 'rejected' : 'updated';
            showAlert(`Leave ${msg} successfully!`, 'success');
            await loadLeaveRequests();
        } else {
            showAlert(result.message || 'Failed to update', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error: ' + error.message, 'error');
    }
}

// Close modals
function closeRejectLeaveModal() {
    document.getElementById('rejectLeaveModal').style.display = 'none';
    currentRejectLeaveId = null;
}

function closeLeaveDetailsModal() {
    document.getElementById('leaveDetailsModal').style.display = 'none';
}

function closeApproveLeaveModal() {
    document.getElementById('approveLeaveModal').style.display = 'none';
    currentApproveLeaveId = null;
}

// Form handlers
document.addEventListener('DOMContentLoaded', function() {
    // Reject form
    const rejectForm = document.getElementById('rejectLeaveForm');
    if (rejectForm) {
        rejectForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            if (!currentRejectLeaveId) {
                showAlert('No leave selected', 'error');
                return;
            }
            const comments = document.getElementById('rejectComments').value.trim();
            if (!comments) {
                showAlert('Please provide a reason', 'error');
                return;
            }
            await updateLeaveStatus(currentRejectLeaveId, 'REJECTED', comments);
            closeRejectLeaveModal();
        });
    }

    // Approve form
    const approveForm = document.getElementById('approveLeaveForm');
    if (approveForm) {
        approveForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            if (!currentApproveLeaveId) {
                showAlert('No leave selected', 'error');
                return;
            }
            await updateLeaveStatus(currentApproveLeaveId, 'APPROVED');
            closeApproveLeaveModal();
        });
    }

    // Click outside modal to close
    window.addEventListener('click', function(event) {
        if (event.target.id === 'rejectLeaveModal') closeRejectLeaveModal();
        if (event.target.id === 'leaveDetailsModal') closeLeaveDetailsModal();
        if (event.target.id === 'approveLeaveModal') closeApproveLeaveModal();
    });

    // Load leaves
    console.log('=== INITIALIZING LEAVE MANAGEMENT ===');
    setTimeout(() => {
        const section = document.getElementById('leaveManagement');
        if (section) {
            loadLeaveRequests();
        }
    }, 500);
});

// Hook into showSection
(function() {
    const original = window.showSection;
    window.showSection = function(sectionId) {
        if (original) original.call(this, sectionId);
        if (sectionId === 'leaveManagement') {
            console.log('Loading leaves...');
            loadLeaveRequests();
        }
    };
})();