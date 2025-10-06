// Communication System - FIXED VERSION with Enhanced Debugging
let currentUserEmail = '';
let currentMessageId = null;
let inboxMessages = [];
let sentMessages = [];
let currentSection = 'compose';

// Initialize communication system
document.addEventListener('DOMContentLoaded', function() {
    console.log('=== INITIALIZING COMMUNICATION SYSTEM ===');
    initializeCommunication();
});

function initializeCommunication() {
    currentUserEmail = document.getElementById('current-user-email')?.value;

    if (!currentUserEmail) {
        console.error('❌ No user email found');
        alert('Please login to access communication system');
        window.location.href = '/systemUserLogin';
        return;
    }

    console.log('✅ Current user email:', currentUserEmail);
    showSection('compose');
    loadAvailableUsers();
    setupFormHandlers();
}

function setupFormHandlers() {
    const composeForm = document.getElementById('compose-form');
    if (composeForm) {
        composeForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const receiverEmail = document.getElementById('receiver-select').value;
            const subject = document.getElementById('message-subject').value;
            const messageText = document.getElementById('message-text').value;

            if (!receiverEmail || !subject || !messageText) {
                showMessage('Please fill in all fields', 'error');
                return;
            }
            sendMessage(receiverEmail, subject, messageText);
        });
    }

    const messageText = document.getElementById('message-text');
    if (messageText) {
        messageText.addEventListener('input', function() {
            document.getElementById('char-count').textContent = this.value.length;
        });
    }
}

function showSection(sectionId, event) {
    console.log('📂 Showing section:', sectionId);
    if (event) event.preventDefault();

    document.querySelectorAll('.communication-section').forEach(section => {
        section.classList.remove('active');
        section.style.display = 'none';
    });

    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
        targetSection.style.display = 'block';
    }

    currentSection = sectionId;

    switch(sectionId) {
        case 'compose':
            // Ensure the compose form is ready
            setTimeout(() => {
                loadAvailableUsers();
                setupFormHandlers();
            }, 50);
            break;
        case 'inbox': loadInbox(); break;
        case 'sent': loadSentMessages(); break;
        case 'statistics': loadStatistics(); break;
    }
}

function loadAvailableUsers() {
    console.log('📥 Loading available users...');
    fetch(`/communication/users?currentUserEmail=${encodeURIComponent(currentUserEmail)}`)
        .then(response => response.json())
        .then(data => {
            console.log('👥 Users response:', data);
            if (data.success && data.users) {
                populateUserDropdown(data.users);
            } else {
                console.error('❌ Failed to load users:', data);
            }
        })
        .catch(error => console.error('❌ Error loading users:', error));
}

function populateUserDropdown(users) {
    const dropdown = document.getElementById('receiver-select');
    if (!dropdown) return;

    dropdown.innerHTML = '<option value="">Select recipient...</option>';
    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.email;
        option.textContent = `${user.user_name} (${user.role})`;
        dropdown.appendChild(option);
    });
    console.log(`✅ Loaded ${users.length} users into dropdown`);
}

function sendMessage(receiverEmail, subject, messageText) {
    console.log('📤 Sending message to:', receiverEmail);
    const submitBtn = document.querySelector('#compose-form button[type="submit"]');
    const originalText = submitBtn.innerHTML;

    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
    submitBtn.disabled = true;

    const formData = new URLSearchParams();
    formData.append('senderEmail', currentUserEmail);
    formData.append('receiverEmail', receiverEmail);
    formData.append('subject', subject);
    formData.append('messageText', messageText);

    fetch('/communication/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log('✉️ Send response:', data);
            if (data.success) {
                showMessage('Message sent successfully!', 'success');
                clearComposeForm();
                if (currentSection === 'sent') loadSentMessages();
            } else {
                showMessage(data.message || 'Failed to send message', 'error');
            }
        })
        .catch(error => {
            console.error('❌ Send error:', error);
            showMessage('Error sending message', 'error');
        })
        .finally(() => {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        });
}

function clearComposeForm() {
    document.getElementById('compose-form')?.reset();
    document.getElementById('char-count').textContent = '0';
}

function loadInbox() {
    console.log('=== 📨 LOADING INBOX ===');
    console.log('User email:', currentUserEmail);

    showLoadingState('inbox', true);

    fetch(`/communication/inbox?userEmail=${encodeURIComponent(currentUserEmail)}`)
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return response.json();
        })
        .then(data => {
            console.log('📨 Inbox response:', data);
            console.log('Success:', data.success);
            console.log('Messages array:', data.messages);
            console.log('Messages count:', data.messages?.length);

            if (data.success) {
                inboxMessages = data.messages || [];
                console.log(`✅ Loaded ${inboxMessages.length} inbox messages`);
                displayInboxMessages(inboxMessages);
                updateUnreadBadge(data.unreadCount || 0);
            } else {
                console.error('❌ Inbox load failed:', data.message);
                showEmptyState('inbox', 'Error loading inbox');
            }
        })
        .catch(error => {
            console.error('❌ Inbox error:', error);
            showEmptyState('inbox', 'Network error');
        })
        .finally(() => showLoadingState('inbox', false));
}

function displayInboxMessages(messages) {
    console.log('🖼️ Displaying inbox messages:', messages);

    const tableBody = document.getElementById('inbox-body');
    const table = document.getElementById('inbox-table');
    const emptyElement = document.getElementById('inbox-empty');
    const subtitle = document.getElementById('inbox-subtitle');

    if (!tableBody) {
        console.error('❌ Table body not found');
        return;
    }

    if (!messages || messages.length === 0) {
        console.log('📭 No messages to display');
        if (subtitle) subtitle.textContent = '(0 messages)';
        if (table) table.style.display = 'none';
        if (emptyElement) emptyElement.style.display = 'block';
        tableBody.innerHTML = '';
        return;
    }

    console.log(`✅ Rendering ${messages.length} messages`);
    if (subtitle) subtitle.textContent = `(${messages.length} messages)`;

    const messagesHtml = messages.map((msg, index) => {
        console.log(`Message ${index}:`, msg);

        const messageId = msg.message_id;
        const senderName = msg.sender_name || 'Unknown';
        const senderEmail = msg.sender_email || '';
        const senderRole = msg.sender_role || 'Unknown';
        const subject = msg.subject || 'No Subject';
        const preview = msg.message_preview || 'No preview';
        const sentDate = msg.sent_date;
        const isRead = msg.is_read === 1 || msg.is_read === true;

        return `
            <tr class="${isRead ? '' : 'unread'}" onclick="viewMessage(${messageId}, 'inbox')">
                <td>
                    <strong>${escapeHtml(senderName)}</strong>
                    <br><small>${escapeHtml(senderEmail)}</small>
                </td>
                <td><span class="badge role-${senderRole.toLowerCase().replace(/ /g, '-')}">${escapeHtml(senderRole)}</span></td>
                <td><strong>${escapeHtml(subject)}</strong></td>
                <td>${escapeHtml(preview)}</td>
                <td>${formatDateTime(sentDate)}</td>
                <td>
                    <div class="message-actions">
                        <i class="fas ${isRead ? 'fa-envelope-open' : 'fa-envelope'}"></i>
                        <button class="btn btn-sm btn-info" onclick="event.stopPropagation(); viewMessage(${messageId}, 'inbox')">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    tableBody.innerHTML = messagesHtml;
    if (table) table.style.display = 'table';
    if (emptyElement) emptyElement.style.display = 'none';

    console.log('✅ Inbox rendering complete');
}

function loadSentMessages() {
    console.log('=== 📤 LOADING SENT MESSAGES ===');
    showLoadingState('sent', true);

    fetch(`/communication/sent?userEmail=${encodeURIComponent(currentUserEmail)}`)
        .then(response => {
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return response.json();
        })
        .then(data => {
            console.log('📤 Sent response:', data);
            console.log('Messages:', data.messages);

            if (data.success) {
                sentMessages = data.messages || [];
                console.log(`✅ Loaded ${sentMessages.length} sent messages`);
                displaySentMessages(sentMessages);
            } else {
                console.error('❌ Sent load failed:', data.message);
                showEmptyState('sent', 'Error loading sent messages');
            }
        })
        .catch(error => {
            console.error('❌ Sent error:', error);
            showEmptyState('sent', 'Network error');
        })
        .finally(() => showLoadingState('sent', false));
}

function displaySentMessages(messages) {
    console.log('🖼️ Displaying sent messages:', messages);

    const tableBody = document.getElementById('sent-body');
    const table = document.getElementById('sent-table');
    const emptyElement = document.getElementById('sent-empty');

    if (!tableBody) {
        console.error('❌ Sent table body not found');
        return;
    }

    if (!messages || messages.length === 0) {
        console.log('📭 No sent messages');
        if (table) table.style.display = 'none';
        if (emptyElement) emptyElement.style.display = 'block';
        tableBody.innerHTML = '';
        return;
    }

    console.log(`✅ Rendering ${messages.length} sent messages`);

    const messagesHtml = messages.map((msg, index) => {
        console.log(`Sent message ${index}:`, msg);

        const messageId = msg.message_id;
        const receiverName = msg.receiver_name || 'Unknown';
        const receiverEmail = msg.receiver_email || '';
        const receiverRole = msg.receiver_role || 'Unknown';
        const subject = msg.subject || 'No Subject';
        const preview = msg.message_preview || 'No preview';
        const sentDate = msg.sent_date;
        const isRead = msg.is_read === 1 || msg.is_read === true;

        return `
            <tr onclick="viewMessage(${messageId}, 'sent')">
                <td>
                    <strong>${escapeHtml(receiverName)}</strong>
                    <br><small>${escapeHtml(receiverEmail)}</small>
                </td>
                <td><span class="badge role-${receiverRole.toLowerCase().replace(/ /g, '-')}">${escapeHtml(receiverRole)}</span></td>
                <td><strong>${escapeHtml(subject)}</strong></td>
                <td>${escapeHtml(preview)}</td>
                <td>${formatDateTime(sentDate)}</td>
                <td>
                    <span class="status-badge status-${isRead ? 'read' : 'unread'}">
                        ${isRead ? 'Read' : 'Unread'}
                    </span>
                </td>
            </tr>
        `;
    }).join('');

    tableBody.innerHTML = messagesHtml;
    if (table) table.style.display = 'table';
    if (emptyElement) emptyElement.style.display = 'none';

    console.log('✅ Sent rendering complete');
}

function showLoadingState(type, show) {
    const loading = document.getElementById(`${type}-loading`);
    const empty = document.getElementById(`${type}-empty`);
    const table = document.getElementById(`${type}-table`);

    if (show) {
        if (loading) loading.style.display = 'block';
        if (empty) empty.style.display = 'none';
        if (table) table.style.display = 'none';
    } else {
        if (loading) loading.style.display = 'none';
    }
}

function showEmptyState(type, message) {
    const emptyElement = document.getElementById(`${type}-empty`);
    const table = document.getElementById(`${type}-table`);

    if (table) table.style.display = 'none';
    if (emptyElement) {
        emptyElement.style.display = 'block';
        emptyElement.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <h4>${message}</h4>
            <p>Please try again</p>
        `;
    }
}

function loadStatistics() {
    console.log('=== 📊 LOADING STATISTICS ===');

    fetch(`/communication/statistics?userEmail=${encodeURIComponent(currentUserEmail)}`)
        .then(response => response.json())
        .then(data => {
            console.log('📊 Statistics response:', data);
            if (data.success) {
                displayStatistics(data.statistics, data.userSummary);
            } else {
                displayStatistics([], null);
            }
        })
        .catch(error => {
            console.error('❌ Statistics error:', error);
            displayStatistics([], null);
        });

    fetch('/communication/active-communicators')
        .then(response => response.json())
        .then(data => {
            console.log('👥 Active communicators response:', data);
            displayActiveCommunicators(data.success ? data.activeUsers : []);
        })
        .catch(error => console.error('❌ Active communicators error:', error));
}

function displayStatistics(statistics, userSummary) {
    console.log('📊 Displaying statistics:', statistics, userSummary);

    const statsBody = document.getElementById('stats-body');
    if (statsBody) {
        if (statistics && statistics.length > 0) {
            statsBody.innerHTML = statistics.map(stat => `
                <tr>
                    <td>${escapeHtml(stat.role)}</td>
                    <td>${stat.total_messages_sent || 0}</td>
                    <td>${Math.round(stat.avg_message_length || 0)} characters</td>
                </tr>
            `).join('');
        } else {
            statsBody.innerHTML = '<tr><td colspan="3" class="text-center">No statistics available</td></tr>';
        }
    }

    if (userSummary) {
        const totalMsg = document.getElementById('total-messages');
        const unreadMsg = document.getElementById('unread-messages');
        const avgLen = document.getElementById('avg-message-length');

        if (totalMsg) totalMsg.textContent = userSummary.sent_count || 0;
        if (unreadMsg) unreadMsg.textContent = userSummary.unread_count || 0;
        if (avgLen) avgLen.textContent = Math.round(userSummary.avg_message_length || 0) + ' chars';
    }
}

function displayActiveCommunicators(activeUsers) {
    const body = document.getElementById('active-users-body');
    if (!body) return;

    if (activeUsers && activeUsers.length > 0) {
        body.innerHTML = activeUsers.map(user => `
            <tr>
                <td>${escapeHtml(user.user_name)}</td>
                <td>${escapeHtml(user.role)}</td>
                <td>${user.message_count || 0}</td>
            </tr>
        `).join('');
    } else {
        body.innerHTML = '<tr><td colspan="3" class="text-center">No active communicators yet</td></tr>';
    }
}

function viewMessage(messageId, source) {
    console.log(`👁️ Viewing message ${messageId} from ${source}`);
    currentMessageId = messageId;

    fetch(`/communication/message/${messageId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.message) {
                displayMessageModal(data.message, source);
                if (source === 'inbox' && !data.message.is_read) {
                    markMessageAsRead(messageId);
                }
            }
        })
        .catch(error => console.error('❌ View message error:', error));
}

function displayMessageModal(message, source) {
    const modal = document.getElementById('message-modal');
    if (!modal) return;

    document.getElementById('modal-subject').textContent = message.subject;
    document.getElementById('modal-sender').textContent = `${message.sender_name} (${message.sender_email})`;
    document.getElementById('modal-sender-role').textContent = message.sender_role;
    document.getElementById('modal-receiver').textContent = `${message.receiver_name} (${message.receiver_email})`;
    document.getElementById('modal-receiver-role').textContent = message.receiver_role;
    document.getElementById('modal-date').textContent = formatDateTime(message.sent_date);
    document.getElementById('modal-content').textContent = message.message_text;

    document.getElementById('delete-btn').style.display = 'inline-block';
    document.getElementById('reply-btn').style.display = source === 'inbox' ? 'inline-block' : 'none';

    modal.style.display = 'flex';
}

function closeMessageModal() {
    document.getElementById('message-modal').style.display = 'none';
    currentMessageId = null;
}

function markMessageAsRead(messageId) {
    fetch('/communication/mark-read', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `messageId=${messageId}`
    })
        .then(() => { if (currentSection === 'inbox') loadInbox(); })
        .catch(error => console.error('❌ Mark read error:', error));
}

function deleteMessage(messageId) {
    if (!confirm('Delete this message?')) return;

    fetch('/communication/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `messageId=${messageId}`
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showMessage('Message deleted', 'success');
                closeMessageModal();
                if (currentSection === 'inbox') loadInbox();
                else if (currentSection === 'sent') loadSentMessages();
            }
        })
        .catch(error => console.error('❌ Delete error:', error));
}

function deleteCurrentMessage() {
    if (currentMessageId) deleteMessage(currentMessageId);
}

function replyToMessage() {
    if (!currentMessageId) return;

    fetch(`/communication/message/${currentMessageId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const msg = data.message;
                showSection('compose');

                // Wait a brief moment for the compose section to be fully rendered
                setTimeout(() => {
                    const receiverSelect = document.getElementById('receiver-select');
                    const subjectInput = document.getElementById('message-subject');
                    const messageTextarea = document.getElementById('message-text');

                    if (receiverSelect && subjectInput && messageTextarea) {
                        // Set the receiver email
                        receiverSelect.value = msg.sender_email;

                        // Set the subject with "Re: " prefix
                        subjectInput.value = `Re: ${msg.subject}`;

                        // Set the message text with original content
                        messageTextarea.value = `\n\n--- Original ---\nFrom: ${msg.sender_name}\n\n${msg.message_text}`;

                        // Update character count
                        document.getElementById('char-count').textContent = messageTextarea.value.length;

                        console.log('✅ Reply form populated successfully');
                    } else {
                        console.error('❌ Form elements not found');
                        // Retry after a longer delay if elements aren't found
                        setTimeout(() => {
                            const retryReceiverSelect = document.getElementById('receiver-select');
                            const retrySubjectInput = document.getElementById('message-subject');
                            const retryMessageTextarea = document.getElementById('message-text');

                            if (retryReceiverSelect && retrySubjectInput && retryMessageTextarea) {
                                retryReceiverSelect.value = msg.sender_email;
                                retrySubjectInput.value = `Re: ${msg.subject}`;
                                retryMessageTextarea.value = `\n\n--- Original ---\nFrom: ${msg.sender_name}\n\n${msg.message_text}`;
                                document.getElementById('char-count').textContent = retryMessageTextarea.value.length;
                            }
                        }, 500);
                    }
                }, 100);

                closeMessageModal();
            }
        })
        .catch(error => {
            console.error('❌ Error fetching message for reply:', error);
            showMessage('Error loading message for reply', 'error');
        });
}

function updateUnreadBadge(count) {
    const badge = document.getElementById('unread-badge');
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'inline-block' : 'none';
    }
}

function refreshInbox() { loadInbox(); }
function refreshSent() { loadSentMessages(); }

function filterInbox() {
    const term = document.getElementById('inbox-search').value.toLowerCase();
    const filtered = inboxMessages.filter(m =>
        (m.sender_name||'').toLowerCase().includes(term) ||
        (m.subject||'').toLowerCase().includes(term)
    );
    displayInboxMessages(filtered);
}

function filterSent() {
    const term = document.getElementById('sent-search').value.toLowerCase();
    const filtered = sentMessages.filter(m =>
        (m.receiver_name||'').toLowerCase().includes(term) ||
        (m.subject||'').toLowerCase().includes(term)
    );
    displaySentMessages(filtered);
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    try {
        return new Date(dateString).toLocaleString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch { return 'Invalid Date'; }
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return String(unsafe)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function showMessage(message, type) {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed; top: 20px; right: 20px; z-index: 1001;
        min-width: 300px; padding: 15px; border-radius: 5px;
        background: ${type === 'error' ? '#dc3545' : '#28a745'};
        color: white; font-weight: bold;
    `;
    toast.innerHTML = `<i class="fas ${type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle'}"></i> ${message}`;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

window.onclick = e => { if (e.target.id === 'message-modal') closeMessageModal(); }
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeMessageModal(); });