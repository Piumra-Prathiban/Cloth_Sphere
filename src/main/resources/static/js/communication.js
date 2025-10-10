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
        // Remove any existing event listeners to prevent duplicates
        composeForm.replaceWith(composeForm.cloneNode(true));
        const newComposeForm = document.getElementById('compose-form');

        newComposeForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn.disabled) {
                console.log('⚠️ Form submission already in progress');
                return;
            }

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

    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
        section.style.display = 'none';
    });

    // Remove active class from all nav links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });

    // Show target section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
        targetSection.style.display = 'block';
    }

    // Add active class to clicked nav link
    if (event && event.target.tagName === 'A') {
        event.target.classList.add('active');
    }

    currentSection = sectionId;

    // Load section-specific content
    switch(sectionId) {
        case 'compose':
            // Clear form and ensure it's ready
            setTimeout(() => {
                clearComposeForm();
                loadAvailableUsers();
                setupFormHandlers();
                console.log('✅ Compose section initialized');
            }, 50);
            break;
        case 'inbox': loadInbox(); break;
        case 'sent': loadSentMessages(); break;
        case 'guide':
            // Guide content is static, no need to load anything
            break;
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

    // PREVENT MULTIPLE SUBMISSIONS
    if (submitBtn.disabled) {
        console.log('⚠️ Send already in progress, ignoring duplicate click');
        return;
    }

    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
    submitBtn.disabled = true;

    // Create proper form data
    const formData = new FormData();
    formData.append('senderEmail', currentUserEmail);
    formData.append('receiverEmail', receiverEmail);
    formData.append('subject', subject.trim());
    formData.append('messageText', messageText.trim());

    console.log('📦 Sending data:', {
        senderEmail: currentUserEmail,
        receiverEmail: receiverEmail,
        subject: subject.trim()
    });

    fetch('/communication/send', {
        method: 'POST',
        body: formData  // Use FormData instead of URLSearchParams
    })
        .then(response => {
            console.log('📨 Response status:', response.status);
            console.log('📨 Response ok:', response.ok);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status} - ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('✉️ Send response:', data);
            if (data.success) {
                showMessage('Message sent successfully!', 'success');
                clearComposeForm();
                // Refresh sent messages if we're in that section
                if (currentSection === 'sent') {
                    setTimeout(() => loadSentMessages(), 1000);
                }
            } else {
                showMessage(data.message || 'Failed to send message', 'error');
            }
        })
        .catch(error => {
            console.error('❌ Send error:', error);
            console.error('❌ Error details:', error.message);
            showMessage('Network error: ' + error.message, 'error');
        })
        .finally(() => {
            // Re-enable button after a delay to prevent rapid clicking
            setTimeout(() => {
                submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Send Message';
                submitBtn.disabled = false;
            }, 2000);
        });
}

function clearComposeForm() {
    const form = document.getElementById('compose-form');
    if (form) {
        form.reset();
    }
    document.getElementById('char-count').textContent = '0';

    // Ensure send button is enabled
    const submitBtn = document.querySelector('#compose-form button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Send Message';
    }
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
                <td><span class="role-badge role-${receiverRole.toLowerCase().replace(/ /g, '-')}">${escapeHtml(receiverRole)}</span></td>
                <td><strong>${escapeHtml(subject)}</strong></td>
                <td>${escapeHtml(preview)}</td>
                <td>${formatDateTime(sentDate)}</td>
                <td>
                    <span class="status-badge ${isRead ? 'status-read' : 'status-unread'}">
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

    // Validate messageId
    if (!messageId || messageId <= 0) {
        console.error('❌ Invalid messageId:', messageId);
        showMessage('Invalid message ID', 'error');
        return;
    }

    currentMessageId = parseInt(messageId); // Ensure it's a number

    console.log('📡 Fetching message details for ID:', currentMessageId);

    fetch(`/communication/message/${currentMessageId}`)
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status} - ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Message details response:', data);
            if (data.success && data.message) {
                displayMessageModal(data.message, source);
                // MARK AS READ IMMEDIATELY when viewing from inbox
                if (source === 'inbox') {
                    markMessageAsRead(currentMessageId);
                }
            } else {
                console.error('❌ Failed to get message details:', data.message);
                showMessage('Error loading message: ' + (data.message || 'Unknown error'), 'error');
            }
        })
        .catch(error => {
            console.error('❌ View message error:', error);
            showMessage('Error loading message: ' + error.message, 'error');
        });
}

// Update the markMessageAsRead function to refresh inbox
function markMessageAsRead(messageId) {
    fetch('/communication/mark-read', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `messageId=${messageId}`
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log('✅ Message marked as read');
                // Refresh inbox to update the UI
                if (currentSection === 'inbox') {
                    loadInbox();
                }
            }
        })
        .catch(error => console.error('❌ Mark read error:', error));
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
    console.log('🔄 Reply button clicked, currentMessageId:', currentMessageId);

    // Validate currentMessageId
    if (!currentMessageId || currentMessageId <= 0) {
        console.error('❌ No valid message ID for reply');
        showMessage('No message selected for reply', 'error');
        return;
    }

    // Store the message ID before closing modal
    const messageIdToReply = currentMessageId;

    // First close the modal
    closeMessageModal();

    // Switch to compose section
    showSection('compose');

    // Wait a bit for the compose section to load, then populate the form
    setTimeout(() => {
        populateReplyForm(messageIdToReply);
    }, 300);
}

function populateReplyForm(messageId) {
    console.log('📨 Populating reply form for message:', messageId);

    fetch(`/communication/message/${messageId}`)
        .then(response => {
            console.log('Reply fetch response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status} - ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Reply message data:', data);
            if (data.success && data.message) {
                const msg = data.message;

                // Get form elements
                const receiverSelect = document.getElementById('receiver-select');
                const subjectInput = document.getElementById('message-subject');
                const messageTextarea = document.getElementById('message-text');

                if (!receiverSelect || !subjectInput || !messageTextarea) {
                    console.error('❌ Form elements not found');
                    showMessage('Error: Compose form not loaded properly', 'error');
                    return;
                }

                // Set receiver to the original sender
                if (msg.sender_email) {
                    receiverSelect.value = msg.sender_email;
                    console.log('✅ Set receiver to:', msg.sender_email);

                    // Trigger any change events if needed
                    const event = new Event('change', { bubbles: true });
                    receiverSelect.dispatchEvent(event);
                }

                // Set subject with Re: prefix
                const currentSubject = msg.subject || 'No Subject';
                if (!currentSubject.startsWith('Re: ')) {
                    subjectInput.value = `Re: ${currentSubject}`;
                } else {
                    subjectInput.value = currentSubject;
                }
                console.log('✅ Set subject to:', subjectInput.value);

                // Set message content with original message
                const originalMessage = `\n\n--- Original Message ---\nFrom: ${msg.sender_name || 'Unknown'} (${msg.sender_role || 'Unknown'})\nDate: ${formatDateTime(msg.sent_date)}\nSubject: ${msg.subject || 'No Subject'}\n\n${msg.message_text || 'No content'}`;
                messageTextarea.value = originalMessage.trim();

                // Update character count
                const charCount = document.getElementById('char-count');
                if (charCount) {
                    charCount.textContent = messageTextarea.value.length;
                }

                console.log('✅ Set message body, length:', messageTextarea.value.length);

                // Focus on message area
                setTimeout(() => {
                    messageTextarea.focus();
                    messageTextarea.scrollTop = 0;
                }, 100);

                showMessage('Reply form populated successfully', 'success');
            } else {
                console.error('❌ Failed to get message for reply:', data.message);
                showMessage('Error loading message for reply: ' + (data.message || 'Unknown error'), 'error');
            }
        })
        .catch(error => {
            console.error('❌ Error fetching message for reply:', error);
            showMessage('Error loading message for reply: ' + error.message, 'error');
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