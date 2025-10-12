// ============================================================================
// Production Alerts JavaScript
// Handles alert notification functionality for Factory Manager Dashboard
// ============================================================================

let alertDropdownOpen = false;
let alerts = [];

// ===================== INITIALIZATION =====================

/**
 * Initialize alerts on page load
 */
document.addEventListener('DOMContentLoaded', function() {
    loadAlerts();

    // Refresh alerts every 5 minutes
    setInterval(loadAlerts, 5 * 60 * 1000);

    // Close dropdown when clicking outside
    document.addEventListener('click', function(event) {
        const dropdown = document.getElementById('alertDropdown');
        const bell = document.querySelector('.alert-bell');

        if (alertDropdownOpen && dropdown && bell) {
            if (!dropdown.contains(event.target) && !bell.contains(event.target)) {
                closeAlertDropdown();
            }
        }
    });
});

// ===================== ALERT LOADING =====================

/**
 * Load alerts from server
 */
function loadAlerts() {
    fetch('/factory/api/alerts/unread')
        .then(response => response.json())
        .then(data => {
            alerts = data;
            updateAlertBadge();
            if (alertDropdownOpen) {
                renderAlerts();
            }
        })
        .catch(error => {
            console.error('Error loading alerts:', error);
        });
}

/**
 * Update alert badge count
 */
function updateAlertBadge() {
    const badge = document.getElementById('alertBadge');
    if (!badge) return;

    const count = alerts.length;

    if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

// ===================== DROPDOWN CONTROL =====================

/**
 * Toggle alert dropdown visibility
 */
function toggleAlertDropdown() {
    const dropdown = document.getElementById('alertDropdown');
    if (!dropdown) return;

    if (alertDropdownOpen) {
        closeAlertDropdown();
    } else {
        openAlertDropdown();
    }
}

/**
 * Open alert dropdown
 */
function openAlertDropdown() {
    const dropdown = document.getElementById('alertDropdown');
    if (!dropdown) return;

    dropdown.classList.add('show');
    alertDropdownOpen = true;
    renderAlerts();
}

/**
 * Close alert dropdown
 */
function closeAlertDropdown() {
    const dropdown = document.getElementById('alertDropdown');
    if (!dropdown) return;

    dropdown.classList.remove('show');
    alertDropdownOpen = false;
}

// ===================== ALERT RENDERING =====================

/**
 * Render alerts in dropdown
 */
function renderAlerts() {
    const alertList = document.getElementById('alertList');
    if (!alertList) return;

    if (alerts.length === 0) {
        alertList.innerHTML = `
            <div class="alert-empty">
                <i class="fas fa-check-circle"></i>
                <p>No new alerts</p>
            </div>
        `;
        return;
    }

    alertList.innerHTML = alerts.map(alert => createAlertHTML(alert)).join('');
}

/**
 * Create HTML for a single alert
 */
function createAlertHTML(alert) {
    const severityClass = alert.severity.toLowerCase();
    const timeAgo = getTimeAgo(alert.createdAt);
    const unreadClass = !alert.isRead ? 'unread' : '';

    return `
        <div class="alert-item ${unreadClass}" data-alert-id="${alert.alertId}">
            <div class="alert-item-header">
                <span class="alert-severity ${severityClass}">${alert.severity}</span>
                <span class="alert-time">${timeAgo}</span>
            </div>
            <div class="alert-message">
                <i class="fas fa-${getAlertIcon(alert.alertType)}"></i>
                ${alert.message}
            </div>
            <div class="alert-order-info">
                <span><i class="fas fa-box"></i> Order: ${alert.orderId}</span>
                <span><i class="fas fa-calendar"></i> Deadline: ${formatDate(alert.deadline)}</span>
                ${alert.daysRemaining !== null ?
                    `<span><i class="fas fa-clock"></i> ${formatDaysRemaining(alert.daysRemaining)}</span>` : ''}
            </div>
            <div class="alert-actions">
                <button class="alert-dismiss-btn" onclick="dismissAlert('${alert.alertId}', event)">
                    <i class="fas fa-times"></i> Dismiss
                </button>
                <button class="alert-view-btn" onclick="viewOrder('${alert.orderId}')">
                    <i class="fas fa-eye"></i> View Order
                </button>
            </div>
        </div>
    `;
}

/**
 * Get icon for alert type
 */
function getAlertIcon(alertType) {
    const icons = {
        'OVERDUE': 'exclamation-triangle',
        'URGENT': 'exclamation-circle',
        'DEADLINE_WARNING': 'clock',
        'QUALITY_ISSUE': 'tools'
    };
    return icons[alertType] || 'bell';
}

/**
 * Format date
 */
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

/**
 * Format days remaining
 */
function formatDaysRemaining(days) {
    if (days < 0) {
        return `${Math.abs(days)} day(s) overdue`;
    } else if (days === 0) {
        return 'Due today';
    } else if (days === 1) {
        return '1 day remaining';
    } else {
        return `${days} days remaining`;
    }
}

/**
 * Get time ago string
 */
function getTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);

    if (seconds < 60) return 'Just now';

    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;

    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;

    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;

    return formatDate(dateString);
}

// ===================== ALERT ACTIONS =====================

/**
 * Mark alert as read
 */
function markAlertAsRead(alertId) {
    fetch(`/factory/api/alerts/${alertId}/read`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update local alerts array
            const alert = alerts.find(a => a.alertId === alertId);
            if (alert) {
                alert.isRead = true;
            }
            updateAlertBadge();
            renderAlerts();
        }
    })
    .catch(error => {
        console.error('Error marking alert as read:', error);
    });
}

/**
 * Mark all alerts as read
 */
function markAllAlertsAsRead() {
    fetch('/factory/api/alerts/read/all', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alerts.forEach(alert => alert.isRead = true);
            updateAlertBadge();
            renderAlerts();
            loadAlerts(); // Refresh from server
        }
    })
    .catch(error => {
        console.error('Error marking all alerts as read:', error);
    });
}

/**
 * Dismiss alert
 */
function dismissAlert(alertId, event) {
    if (event) {
        event.stopPropagation();
    }

    if (!confirm('Are you sure you want to dismiss this alert?')) {
        return;
    }

    fetch(`/factory/api/alerts/${alertId}/dismiss`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove from local array
            alerts = alerts.filter(a => a.alertId !== alertId);
            updateAlertBadge();
            renderAlerts();

            // Show success message
            showNotification('Alert dismissed successfully', 'success');
        } else {
            showNotification('Failed to dismiss alert', 'error');
        }
    })
    .catch(error => {
        console.error('Error dismissing alert:', error);
        showNotification('Error dismissing alert', 'error');
    });
}

/**
 * View order details
 */
function viewOrder(orderId) {
    // Navigate to production orders section and filter by order ID
    closeAlertDropdown();

    // If you have a function to show orders, call it here
    // For now, we'll just navigate to the production section
    const productionLink = document.querySelector('a[href="#production"]');
    if (productionLink) {
        productionLink.click();

        // Wait for section to load, then filter
        setTimeout(() => {
            const searchInput = document.querySelector('input[placeholder*="Search"]');
            if (searchInput) {
                searchInput.value = orderId;
                searchInput.dispatchEvent(new Event('input'));
            }
        }, 300);
    }
}

/**
 * Show notification message
 */
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background: ${type === 'success' ? '#27ae60' : type === 'error' ? '#e74c3c' : '#3498db'};
        color: white;
        border-radius: 5px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 10000;
        animation: slideIn 0.3s ease;
    `;
    notification.textContent = message;

    document.body.appendChild(notification);

    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Add animation styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);
