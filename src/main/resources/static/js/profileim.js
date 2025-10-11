// Mobile menu functionality
const mobileMenuBtn = document.getElementById('mobileMenuBtn');
const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const menuToggle = document.getElementById('menuToggle');

mobileMenuBtn?.addEventListener('click', () => {
    sidebar.classList.add('open');
    sidebarOverlay.classList.add('show');
});

menuToggle?.addEventListener('click', () => {
    sidebar.classList.remove('open');
    sidebarOverlay.classList.remove('show');
});

sidebarOverlay?.addEventListener('click', () => {
    sidebar.classList.remove('open');
    sidebarOverlay.classList.remove('show');
});

// Toggle password visibility
function togglePassword(fieldId) {
    const field = document.getElementById(fieldId);
    const button = field.parentElement.querySelector('.toggle-password i');

    if (field.type === 'password') {
        field.type = 'text';
        button.classList.remove('fa-eye');
        button.classList.add('fa-eye-slash');
    } else {
        field.type = 'password';
        button.classList.remove('fa-eye-slash');
        button.classList.add('fa-eye');
    }
}

// Show notification
function showNotification(message, type = 'success') {
    // Remove any existing notifications
    const existing = document.querySelector('.notification');
    if (existing) {
        existing.remove();
    }

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;

    // Add inline styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        display: flex;
        align-items: center;
        gap: 10px;
        transform: translateX(400px);
        transition: transform 0.3s ease;
        max-width: 400px;
    `;

    if (type === 'success') {
        notification.style.background = 'linear-gradient(135deg, #27ae60, #2ecc71)';
    } else {
        notification.style.background = 'linear-gradient(135deg, #e74c3c, #c0392b)';
    }

    document.body.appendChild(notification);

    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);

    // Animate out and remove
    setTimeout(() => {
        notification.style.transform = 'translateX(400px)';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Display message from session on page load
function displaySessionMessage() {
    const messageInput = document.getElementById('updateMessage');
    if (messageInput && messageInput.value) {
        const message = messageInput.value;
        const parts = message.split(':');

        if (parts.length === 2) {
            const type = parts[0]; // 'success' or 'error'
            const text = parts[1];
            showNotification(text, type);

            // Clear the message from session
            fetch('/user/clearMessage', {
                method: 'POST'
            }).catch(err => console.error('Error clearing message:', err));
        }
    }
}

// Update profile form - CLIENT-SIDE VALIDATION ONLY
const profileForm = document.getElementById('profileForm');
if (profileForm) {
    profileForm.addEventListener('submit', function(e) {
        const email = document.getElementById('email').value.trim();
        const phoneNumber = document.getElementById('phoneNumber').value.trim();

        // Validate email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            e.preventDefault();
            showNotification('Please enter a valid email address', 'error');
            return false;
        }

        // Validate phone
        const phoneRegex = /^[+]?[0-9]{10,15}$/;
        if (!phoneRegex.test(phoneNumber)) {
            e.preventDefault();
            showNotification('Phone number must be 10-15 digits', 'error');
            return false;
        }

        // If validation passes, form will submit normally to /user/update
        return true;
    });
}

// Change password form - CLIENT-SIDE VALIDATION ONLY
const passwordForm = document.getElementById('passwordForm');
if (passwordForm) {
    passwordForm.addEventListener('submit', function(e) {
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Validate password length
        if (newPassword.length < 8) {
            e.preventDefault();
            showNotification('New password must be at least 8 characters long', 'error');
            return false;
        }

        // Check if passwords match
        if (newPassword !== confirmPassword) {
            e.preventDefault();
            showNotification('New passwords do not match', 'error');
            return false;
        }

        // Check if new password is different from current
        if (currentPassword === newPassword) {
            e.preventDefault();
            showNotification('New password must be different from current password', 'error');
            return false;
        }

        // If validation passes, form will submit normally to /user/updatePassword
        return true;
    });
}

// Run on page load
document.addEventListener('DOMContentLoaded', function() {
    displaySessionMessage();
});