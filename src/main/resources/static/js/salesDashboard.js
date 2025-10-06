// Navigation functionality
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
    if (sectionId === 'orderView') {
        loadOrders();
    } else if (sectionId === 'summaryReport') {
        loadSummaryReport();
    } else if (sectionId === 'customerMessage') {
        loadCustomers();
        loadMessageHistory();
    } else if (sectionId === 'customerManagement') {
        loadAllBuyers();
    } else if (sectionId === 'createOrder') {
        loadBuyersForSelection();
    }
}

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log('Sales Dashboard initialized');

    // Initialize sales chart
    initializeSalesChart();

    // Load initial data
    loadQuickStats();

    // Set up form event listeners
    setupFormListeners();

    // Load buyers for selection
    loadBuyersForSelection();
});

// Sales Chart
function initializeSalesChart() {
    const ctx = document.getElementById('salesChart').getContext('2d');
    const salesChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [{
                label: 'Sales Performance',
                data: [12000, 19000, 15000, 25000, 22000, 30000],
                borderColor: '#4caf50',
                backgroundColor: 'rgba(76, 175, 80, 0.1)',
                borderWidth: 2,
                fill: true
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: true
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

// Quick Stats
async function loadQuickStats() {
    try {
        // Simulate API call
        const stats = {
            totalOrders: 15,
            pendingOrders: 3,
            completedOrders: 12,
            successRate: '85%'
        };

        document.getElementById('totalOrders').textContent = stats.totalOrders;
        document.getElementById('pendingOrders').textContent = stats.pendingOrders;
        document.getElementById('completedOrders').textContent = stats.completedOrders;
        document.getElementById('successRate').textContent = stats.successRate;

    } catch (error) {
        console.error('Error loading quick stats:', error);
    }
}

// Form setup
function setupFormListeners() {
    // Order form calculations
    const quantityInput = document.getElementById('quantity');
    const unitPriceInput = document.getElementById('unitPrice');
    const totalAmountInput = document.getElementById('totalAmount');

    function calculateTotal() {
        const quantity = parseInt(quantityInput.value) || 0;
        const unitPrice = parseFloat(unitPriceInput.value) || 0;
        const total = quantity * unitPrice;
        totalAmountInput.value = total.toFixed(2);
    }

    quantityInput.addEventListener('input', calculateTotal);
    unitPriceInput.addEventListener('input', calculateTotal);

    // Order form submission
    document.getElementById('orderForm').addEventListener('submit', function(e) {
        e.preventDefault();
        createOrder();
    });

    // Message form submission
    document.getElementById('messageForm').addEventListener('submit', function(e) {
        e.preventDefault();
        sendMessage();
    });
}

// Order Management
async function loadOrders() {
    try {
        // Simulate API call to get orders
        const orders = [
            {
                id: 'ORD-001',
                customerName: 'John Doe',
                productType: 'T-Shirt',
                quantity: 50,
                totalAmount: 1250.00,
                orderDate: '2024-01-15',
                status: 'COMPLETED'
            },
            {
                id: 'ORD-002',
                customerName: 'Jane Smith',
                productType: 'Shirt',
                quantity: 30,
                totalAmount: 900.00,
                orderDate: '2024-01-16',
                status: 'IN_PROGRESS'
            },
            {
                id: 'ORD-003',
                customerName: 'Bob Johnson',
                productType: 'Pants',
                quantity: 25,
                totalAmount: 750.00,
                orderDate: '2024-01-17',
                status: 'PENDING'
            }
        ];

        refreshOrdersTable(orders);

    } catch (error) {
        console.error('Error loading orders:', error);
        showAlert('Error loading orders', 'error');
    }
}

function refreshOrdersTable(orders) {
    const tbody = document.querySelector('#ordersTable tbody');
    tbody.innerHTML = '';

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">No orders found</td></tr>';
        return;
    }

    orders.forEach(order => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${order.id}</td>
            <td>${order.customerName}</td>
            <td>${order.productType}</td>
            <td>${order.quantity}</td>
            <td>$${order.totalAmount.toFixed(2)}</td>
            <td>${new Date(order.orderDate).toLocaleDateString()}</td>
            <td><span class="status-badge status-${order.status.toLowerCase().replace('_', '-')}">${order.status.replace('_', ' ')}</span></td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="viewOrder('${order.id}')">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-primary btn-sm" onclick="editOrder('${order.id}')">
                    <i class="fas fa-edit"></i> Edit
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function filterOrders() {
    const statusFilter = document.getElementById('orderStatusFilter').value;
    const searchTerm = document.getElementById('orderSearch').value.toLowerCase();

    // In a real application, you would make an API call here
    // For now, we'll simulate filtering
    console.log('Filtering orders:', { statusFilter, searchTerm });
    loadOrders(); // Reload with filters applied
}

async function createOrder() {
    try {
        const formData = new FormData(document.getElementById('orderForm'));
        const orderData = {
            customerName: formData.get('customerName'),
            customerEmail: formData.get('customerEmail'),
            customerPhone: formData.get('customerPhone'),
            customerAddress: formData.get('customerAddress'),
            productType: formData.get('productType'),
            quantity: parseInt(formData.get('quantity')),
            unitPrice: parseFloat(formData.get('unitPrice')),
            totalAmount: parseFloat(formData.get('totalAmount')),
            orderNotes: formData.get('orderNotes')
        };

        // Simulate API call
        console.log('Creating order:', orderData);

        showAlert('Order created successfully!', 'success');
        resetOrderForm();

        // Refresh orders list if on order view
        if (document.getElementById('orderView').classList.contains('active')) {
            loadOrders();
        }

    } catch (error) {
        console.error('Error creating order:', error);
        showAlert('Error creating order', 'error');
    }
}

function resetOrderForm() {
    document.getElementById('orderForm').reset();
    document.getElementById('totalAmount').value = '';
}

function viewOrder(orderId) {
    alert(`View order: ${orderId}`);
    // In a real application, you would show order details in a modal
}

function editOrder(orderId) {
    alert(`Edit order: ${orderId}`);
    // In a real application, you would load order data into the form for editing
}

// Summary Report
async function loadSummaryReport() {
    try {
        // Simulate API call for report data
        const reportData = {
            totalSales: 12450,
            ordersCount: 45,
            customersCount: 28,
            successRate: '92%'
        };

        document.getElementById('totalSales').textContent = '$' + reportData.totalSales.toLocaleString();
        document.getElementById('ordersCount').textContent = reportData.ordersCount;
        document.getElementById('customersCount').textContent = reportData.customersCount;
        document.getElementById('successRateReport').textContent = reportData.successRate;

        initializeReportChart();
        loadTopProducts();

    } catch (error) {
        console.error('Error loading summary report:', error);
    }
}

function initializeReportChart() {
    const ctx = document.getElementById('reportChart').getContext('2d');
    const reportChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
            datasets: [{
                label: 'Sales ($)',
                data: [2800, 3200, 4100, 2350],
                backgroundColor: [
                    'rgba(76, 175, 80, 0.8)',
                    'rgba(33, 150, 243, 0.8)',
                    'rgba(255, 193, 7, 0.8)',
                    'rgba(156, 39, 176, 0.8)'
                ],
                borderColor: [
                    'rgba(76, 175, 80, 1)',
                    'rgba(33, 150, 243, 1)',
                    'rgba(255, 193, 7, 1)',
                    'rgba(156, 39, 176, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function loadTopProducts() {
    const products = [
        { name: 'T-Shirt', sales: 12500, units: 250 },
        { name: 'Shirt', sales: 9800, units: 140 },
        { name: 'Pants', sales: 7500, units: 100 },
        { name: 'Jacket', sales: 5200, units: 65 }
    ];

    const productsList = document.querySelector('.products-list');
    productsList.innerHTML = '';

    products.forEach(product => {
        const productItem = document.createElement('div');
        productItem.className = 'product-item';
        productItem.innerHTML = `
            <div>
                <strong>${product.name}</strong>
                <div style="font-size: 12px; color: #666;">${product.units} units sold</div>
            </div>
            <div style="font-weight: bold; color: #4caf50;">$${product.sales.toLocaleString()}</div>
        `;
        productsList.appendChild(productItem);
    });
}

function updateReport() {
    const reportType = document.getElementById('reportType').value;
    const reportMonth = document.getElementById('reportMonth').value;

    console.log('Updating report:', { reportType, reportMonth });
    loadSummaryReport(); // Reload with new filters
}

function generateReport() {
    alert('Exporting report...');
    // In a real application, you would generate and download a PDF/Excel report
}

// Customer Messaging
async function loadCustomers() {
    try {
        // Simulate API call to get customers
        const customers = [
            { id: 1, name: 'John Doe', email: 'john@example.com' },
            { id: 2, name: 'Jane Smith', email: 'jane@example.com' },
            { id: 3, name: 'Bob Johnson', email: 'bob@example.com' }
        ];

        const customerSelect = document.getElementById('customerSelect');
        customerSelect.innerHTML = '<option value="">Select Customer</option>';

        customers.forEach(customer => {
            const option = document.createElement('option');
            option.value = customer.id;
            option.textContent = `${customer.name} (${customer.email})`;
            customerSelect.appendChild(option);
        });

    } catch (error) {
        console.error('Error loading customers:', error);
    }
}

async function loadMessageHistory() {
    try {
        // Simulate API call to get message history
        const messages = [
            {
                date: '2024-01-15',
                customerName: 'John Doe',
                subject: 'Order Confirmation',
                type: 'ORDER_UPDATE',
                status: 'SENT'
            },
            {
                date: '2024-01-14',
                customerName: 'Jane Smith',
                subject: 'Special Offer',
                type: 'PROMOTIONAL',
                status: 'SENT'
            }
        ];

        refreshMessagesTable(messages);

    } catch (error) {
        console.error('Error loading message history:', error);
    }
}

function refreshMessagesTable(messages) {
    const tbody = document.querySelector('#messagesTable tbody');
    tbody.innerHTML = '';

    if (messages.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No messages found</td></tr>';
        return;
    }

    messages.forEach(message => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${new Date(message.date).toLocaleDateString()}</td>
            <td>${message.customerName}</td>
            <td>${message.subject}</td>
            <td>${message.type.replace('_', ' ')}</td>
            <td><span class="status-badge status-completed">${message.status}</span></td>
        `;
        tbody.appendChild(row);
    });
}

async function sendMessage() {
    try {
        const formData = new FormData(document.getElementById('messageForm'));
        const messageData = {
            customerId: formData.get('customerId'),
            subject: formData.get('subject'),
            content: formData.get('content'),
            messageType: formData.get('messageType')
        };

        // Simulate API call
        console.log('Sending message:', messageData);

        showAlert('Message sent successfully!', 'success');
        resetMessageForm();
        loadMessageHistory(); // Refresh message history

    } catch (error) {
        console.error('Error sending message:', error);
        showAlert('Error sending message', 'error');
    }
}

function resetMessageForm() {
    document.getElementById('messageForm').reset();
}

//=========================================================================
// Customer Management Functions
async function createCustomer() {
    try {
        const customerData = {
            customerName: document.getElementById('customerName').value,
            email: document.getElementById('customerEmail').value,
            phone: document.getElementById('customerPhone').value,
            address: document.getElementById('customerAddress').value,
            company: document.getElementById('company').value || ''
        };

        // Validation
        if (!customerData.customerName || !customerData.email || !customerData.phone || !customerData.address) {
            showAlert('Please fill in all required customer fields', 'error');
            return;
        }

        const response = await fetch('/api/buyers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(customerData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to create customer');
        }

        const createdCustomer = await response.json();
        showAlert('Customer created successfully! Buyer ID: ' + createdCustomer.buyerId, 'success');

        // Refresh customer dropdown
        await loadBuyersForSelection();

        // Auto-fill the form with created customer data
        document.getElementById('customerName').value = createdCustomer.customerName;
        document.getElementById('customerEmail').value = createdCustomer.email;
        document.getElementById('customerPhone').value = createdCustomer.phone;
        document.getElementById('customerAddress').value = createdCustomer.address;
        document.getElementById('company').value = createdCustomer.company || '';

    } catch (error) {
        console.error('Error creating customer:', error);
        showAlert('Error creating customer: ' + error.message, 'error');
    }
}

async function loadBuyersForSelection() {
    try {
        const response = await fetch('/api/buyers');
        if (!response.ok) throw new Error('Failed to load customers');

        const buyers = await response.json();

        const existingCustomerSelect = document.getElementById('existingCustomer');
        if (existingCustomerSelect) {
            existingCustomerSelect.innerHTML = '<option value="">Select Existing Customer</option>';

            buyers.forEach(buyer => {
                const option = document.createElement('option');
                option.value = buyer.email;
                option.textContent = `${buyer.buyerId} - ${buyer.customerName} (${buyer.email})`;
                option.setAttribute('data-buyer', JSON.stringify(buyer));
                existingCustomerSelect.appendChild(option);
            });
        }

        // Clear the Customer Information form
        clearCustomerForm();

        showAlert('Customer list refreshed and form cleared!', 'success');

    } catch (error) {
        console.error('Error loading buyers for selection:', error);
        showAlert('Error loading customers: ' + error.message, 'error');
    }
}

// Function to clear the customer form
function clearCustomerForm() {
    document.getElementById('customerName').value = '';
    document.getElementById('customerEmail').value = '';
    document.getElementById('customerPhone').value = '';
    document.getElementById('customerAddress').value = '';
    document.getElementById('company').value = '';
    document.getElementById('existingCustomer').value = '';
}

function loadCustomerData() {
    const select = document.getElementById('existingCustomer');
    const selectedOption = select.options[select.selectedIndex];

    if (selectedOption.value) {
        const buyer = JSON.parse(selectedOption.getAttribute('data-buyer'));

        // Auto-fill the form with selected customer data
        document.getElementById('customerName').value = buyer.customerName;
        document.getElementById('customerEmail').value = buyer.email;
        document.getElementById('customerPhone').value = buyer.phone || '';
        document.getElementById('customerAddress').value = buyer.address || '';
        document.getElementById('company').value = buyer.company || '';

        showAlert(`Loaded customer: ${buyer.customerName}`, 'success');
    }
}

async function loadAllBuyers() {
    try {
        const response = await fetch('/api/buyers');
        if (!response.ok) throw new Error('Failed to load customers');

        const buyers = await response.json();
        refreshCustomersTable(buyers);

    } catch (error) {
        console.error('Error loading buyers:', error);
        showAlert('Error loading customers', 'error');
    }
}

function refreshCustomersTable(buyers) {
    const tbody = document.querySelector('#customersTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (buyers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No customers found</td></tr>';
        return;
    }

    buyers.forEach(buyer => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${buyer.buyerId}</td>
            <td>${buyer.customerName}</td>
            <td>${buyer.email}</td>
            <td>${buyer.phone || 'N/A'}</td>
            <td>${buyer.company || 'N/A'}</td>
            <td>${buyer.address ? buyer.address.substring(0, 50) + (buyer.address.length > 50 ? '...' : '') : 'N/A'}</td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="editCustomer('${buyer.buyerId}', '${buyer.email}')">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteCustomer('${buyer.buyerId}', '${buyer.email}')">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function editCustomer(buyerId, email) {
    try {
        // Fetch customer details
        const response = await fetch(`/api/buyers/${buyerId}/${encodeURIComponent(email)}`);
        if (!response.ok) throw new Error('Failed to load customer details');

        const customer = await response.json();

        // Show edit form (you can use a modal or inline editing)
        const newName = prompt('Enter new customer name:', customer.customerName);
        if (newName === null) return;

        const newPhone = prompt('Enter new phone:', customer.phone);
        if (newPhone === null) return;

        const newAddress = prompt('Enter new address:', customer.address);
        if (newAddress === null) return;

        const newCompany = prompt('Enter new company:', customer.company);

        const updateData = {
            customerName: newName,
            phone: newPhone,
            address: newAddress,
            company: newCompany || ''
        };

        const updateResponse = await fetch(`/api/buyers/${buyerId}/${encodeURIComponent(email)}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });

        if (!updateResponse.ok) throw new Error('Failed to update customer');

        showAlert('Customer updated successfully!', 'success');
        loadAllBuyers(); // Refresh the table

    } catch (error) {
        console.error('Error editing customer:', error);
        showAlert('Error updating customer: ' + error.message, 'error');
    }
}

async function deleteCustomer(buyerId, email) {
    if (!confirm(`Are you sure you want to delete customer ${buyerId}?`)) {
        return;
    }

    try {
        const response = await fetch(`/api/buyers/${buyerId}/${encodeURIComponent(email)}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to delete customer');
        }

        showAlert('Customer deleted successfully!', 'success');
        loadAllBuyers(); // Refresh the table

        // Also refresh the customer dropdown in create order form
        loadBuyersForSelection();

    } catch (error) {
        console.error('Error deleting customer:', error);
        showAlert('Error deleting customer: ' + error.message, 'error');
    }
}

function filterCustomers() {
    const searchTerm = document.getElementById('customerSearch').value.toLowerCase();
    const rows = document.querySelectorAll('#customersTable tbody tr');

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

// Utility Functions
function showAlert(message, type = 'success') {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());

    // Create new alert
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;

    // Insert after header
    const header = document.querySelector('.dashboard-header');
    header.parentNode.insertBefore(alertDiv, header.nextSibling);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.parentNode.removeChild(alertDiv);
        }
    }, 5000);
}

function resetForm() {
    document.getElementById('passwordForm').reset();
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = '/logout';
    }
}

// Export functions for global access
window.showSection = showSection;
window.logout = logout;
window.resetForm = resetForm;
window.filterOrders = filterOrders;
window.createOrder = createOrder;
window.resetOrderForm = resetOrderForm;
window.viewOrder = viewOrder;
window.editOrder = editOrder;
window.updateReport = updateReport;
window.generateReport = generateReport;
window.sendMessage = sendMessage;
window.resetMessageForm = resetMessageForm;

// Export customer management functions
window.createCustomer = createCustomer;
window.loadBuyersForSelection = loadBuyersForSelection;
window.loadCustomerData = loadCustomerData;
window.loadAllBuyers = loadAllBuyers;
window.editCustomer = editCustomer;
window.deleteCustomer = deleteCustomer;
window.filterCustomers = filterCustomers;