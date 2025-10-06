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
        loadProducts(); // Load products when create order section is shown
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

    // Load products for product dropdown
    loadProducts();
    setupProductSelection();
});

// Sales Chart
function initializeSalesChart() {
    const ctx = document.getElementById('salesChart')?.getContext('2d');
    if (!ctx) return;

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
        const response = await fetch('/api/orders');
        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();

        const totalOrders = orders.length;
        const pendingOrders = orders.filter(order => order.status === 'PENDING').length;
        const completedOrders = orders.filter(order => order.status === 'READY_TO_SHIP').length;
        const successRate = totalOrders > 0 ? Math.round((completedOrders / totalOrders) * 100) : 0;

        document.getElementById('totalOrders').textContent = totalOrders;
        document.getElementById('pendingOrders').textContent = pendingOrders;
        document.getElementById('completedOrders').textContent = completedOrders;
        document.getElementById('successRate').textContent = successRate + '%';

    } catch (error) {
        console.error('Error loading quick stats:', error);
    }
}

// Form setup
function setupFormListeners() {
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

// Load products for product type dropdown
async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        if (!response.ok) throw new Error('Failed to load products');

        const products = await response.json();
        populateProductDropdown(products);

    } catch (error) {
        console.error('Error loading products:', error);
        showAlert('Error loading products: ' + error.message, 'error');
        // Fallback to static products if API fails
        loadFallbackProducts();
    }
}

// Fallback product data in case API fails
function loadFallbackProducts() {
    const fallbackProducts = [
        { name: "Classic Cotton T-Shirt", category: "Shirts", price: 19.99, code: "75001", productId: "P01" },
        { name: "Premium V-Neck Tee", category: "Shirts", price: 24.99, code: "75002", productId: "P02" },
        { name: "Graphic Print T-Shirt", category: "Shirts", price: 29.99, code: "75003", productId: "P03" },
        { name: "Slim Fit Jeans", category: "Jeans", price: 59.99, code: "JN001", productId: "P04" },
        { name: "Relaxed Fit Jeans", category: "Jeans", price: 49.99, code: "JN002", productId: "P05" },
        { name: "Designer Ripped Jeans", category: "Jeans", price: 79.99, code: "JN003", productId: "P06" },
        { name: "Summer Floral Dress", category: "Dresses", price: 45.99, code: "DR001", productId: "P07" },
        { name: "Elegant Cocktail Dress", category: "Dresses", price: 89.99, code: "DR002", productId: "P08" },
        { name: "Denim Jacket", category: "Jackets", price: 65.99, code: "JK001", productId: "P09" },
        { name: "Bomber Jacket", category: "Jackets", price: 75.99, code: "JK002", productId: "P10" }
    ];

    populateProductDropdown(fallbackProducts);
    showAlert('Using fallback product data. Some features may be limited.', 'warning');
}

// Populate product type dropdown with actual products
function populateProductDropdown(products) {
    const productTypeSelect = document.getElementById('productType');
    if (!productTypeSelect) return;

    // Clear existing options except the first one
    productTypeSelect.innerHTML = '<option value="">Select Product Type</option>';

    // Group products by category
    const productsByCategory = {};
    products.forEach(product => {
        if (!productsByCategory[product.category]) {
            productsByCategory[product.category] = [];
        }
        productsByCategory[product.category].push(product);
    });

    // Add products organized by category
    Object.keys(productsByCategory).forEach(category => {
        const optgroup = document.createElement('optgroup');
        optgroup.label = category;

        productsByCategory[category].forEach(product => {
            const option = document.createElement('option');
            option.value = product.name;
            option.textContent = `${product.name} - $${product.price} (Code: ${product.code})`;
            option.setAttribute('data-product', JSON.stringify(product));
            optgroup.appendChild(option);
        });

        productTypeSelect.appendChild(optgroup);
    });
}

// Setup product selection handler
function setupProductSelection() {
    const productTypeSelect = document.getElementById('productType');
    if (productTypeSelect) {
        productTypeSelect.addEventListener('change', function() {
            const selectedOption = this.options[this.selectedIndex];
            if (selectedOption.value && selectedOption.getAttribute('data-product')) {
                const product = JSON.parse(selectedOption.getAttribute('data-product'));
                autoFillProductDetails(product);
            } else {
                // Clear product details if no product selected
                document.getElementById('unitPrice').value = '';
                calculateTotal();
            }
        });
    }
}

// Auto-fill product details when product is selected
function autoFillProductDetails(product) {
    document.getElementById('unitPrice').value = product.price;

    // Set quantity to 1 by default if empty
    const quantityInput = document.getElementById('quantity');
    if (!quantityInput.value || quantityInput.value === '0') {
        quantityInput.value = '1';
    }

    // Trigger calculation
    calculateTotal();

    showAlert(`Product "${product.name}" selected. Price: $${product.price}`, 'success');
}

// Calculate order total with discount
function calculateTotal() {
    const quantity = parseInt(document.getElementById('quantity').value) || 0;
    const unitPrice = parseFloat(document.getElementById('unitPrice').value) || 0;
    const discountPercentage = parseFloat(document.getElementById('discountPercentage').value) || 0;

    const subtotal = quantity * unitPrice;
    const discountAmount = subtotal * (discountPercentage / 100);
    const total = subtotal - discountAmount;

    document.getElementById('subtotalAmount').value = subtotal.toFixed(2);
    document.getElementById('totalAmount').value = total.toFixed(2);
}

// Order Management
async function loadOrders() {
    try {
        const response = await fetch('/api/orders');
        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();
        refreshOrdersTable(orders);

    } catch (error) {
        console.error('Error loading orders:', error);
        showAlert('Error loading orders', 'error');
    }
}

function refreshOrdersTable(orders) {
    const tbody = document.querySelector('#ordersTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" style="text-align: center;">No orders found</td></tr>';
        return;
    }

    orders.forEach(order => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${order.orderId}</td>
            <td>${order.orderType}</td>
            <td>${order.customerName}</td>
            <td>${order.productType}</td>
            <td>${order.quantity}</td>
            <td>$${order.unitPrice.toFixed(2)}</td>
            <td>${order.discountPercentage || 0}%</td>
            <td>$${order.totalAmount.toFixed(2)}</td>
            <td>${new Date(order.placeDate).toLocaleDateString()}</td>
            <td><span class="status-badge status-${order.status.toLowerCase().replace('_', '-')}">${order.status.replace('_', ' ')}</span></td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="viewOrder('${order.orderType}', '${order.orderId}')">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-primary btn-sm" onclick="updateOrderStatus('${order.orderType}', '${order.orderId}')">
                    <i class="fas fa-edit"></i> Update Status
                </button>
                <button class="btn btn-info btn-sm" onclick="updateOrderDiscount('${order.orderType}', '${order.orderId}')">
                    <i class="fas fa-percent"></i> Discount
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
        const productTypeSelect = document.getElementById('productType');
        const selectedProduct = productTypeSelect.options[productTypeSelect.selectedIndex];

        if (!selectedProduct.value) {
            showAlert('Please select a product', 'error');
            return;
        }

        const productData = JSON.parse(selectedProduct.getAttribute('data-product'));

        const orderData = {
            customerName: formData.get('customerName'),
            customerEmail: formData.get('customerEmail'),
            customerPhone: formData.get('customerPhone'),
            customerAddress: formData.get('customerAddress'),
            productType: productData.name,
            quantity: parseInt(formData.get('quantity')),
            unitPrice: parseFloat(formData.get('unitPrice')),
            discountPercentage: parseFloat(formData.get('discountPercentage')) || 0,
            totalAmount: parseFloat(formData.get('totalAmount')),
            orderType: 'PHYSICAL',
            orderNotes: `Product: ${productData.name}, Code: ${productData.code}, Category: ${productData.category}`
        };

        // Validation
        if (!orderData.customerName || !orderData.customerEmail || !orderData.customerPhone) {
            showAlert('Please fill in all required customer fields', 'error');
            return;
        }

        if (orderData.quantity <= 0) {
            showAlert('Please enter a valid quantity', 'error');
            return;
        }

        const response = await fetch('/api/orders?createdBy=SalesManager', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(orderData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to create order');
        }

        const createdOrder = await response.json();
        showAlert('Order created successfully! Order ID: ' + createdOrder.orderId, 'success');
        resetOrderForm();

        // Refresh orders list if on order view
        if (document.getElementById('orderView').classList.contains('active')) {
            loadOrders();
        }

        // Refresh quick stats
        loadQuickStats();

    } catch (error) {
        console.error('Error creating order:', error);
        showAlert('Error creating order: ' + error.message, 'error');
    }
}

function resetOrderForm() {
    document.getElementById('orderForm').reset();
    document.getElementById('subtotalAmount').value = '';
    document.getElementById('totalAmount').value = '';
    document.getElementById('existingCustomer').value = '';
}

async function viewOrder(orderType, orderId) {
    try {
        const response = await fetch(`/api/orders/${orderType}/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();

        // Show order details in a modal or alert
        const orderDetails = `
Order ID: ${order.orderId}
Order Type: ${order.orderType}
Customer: ${order.customerName}
Product: ${order.productType}
Quantity: ${order.quantity}
Unit Price: $${order.unitPrice}
Discount: ${order.discountPercentage || 0}%
Total Amount: $${order.totalAmount}
Status: ${order.status}
Order Date: ${new Date(order.placeDate).toLocaleDateString()}
Notes: ${order.orderNotes || 'None'}
        `;

        alert(orderDetails);
    } catch (error) {
        console.error('Error viewing order:', error);
        showAlert('Error loading order details', 'error');
    }
}

async function updateOrderStatus(orderType, orderId) {
    try {
        const response = await fetch(`/api/orders/${orderType}/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();

        // Determine available status transitions
        let availableStatuses = [];
        if (order.status === 'PENDING') {
            availableStatuses = ['IN_PRODUCTION', 'READY_TO_SHIP'];
        } else if (order.status === 'IN_PRODUCTION') {
            availableStatuses = ['READY_TO_SHIP'];
        } else {
            showAlert('Order status cannot be changed from ' + order.status, 'error');
            return;
        }

        const newStatus = prompt(
            `Current Status: ${order.status}\nAvailable statuses: ${availableStatuses.join(', ')}\nEnter new status:`,
            availableStatuses[0]
        );

        if (!newStatus || !availableStatuses.includes(newStatus)) {
            showAlert('Invalid status selected', 'error');
            return;
        }

        const updateResponse = await fetch(`/api/orders/${orderType}/${orderId}/status?newStatus=${newStatus}&updatedBy=SalesManager`, {
            method: 'PUT'
        });

        if (!updateResponse.ok) {
            const errorData = await updateResponse.json();
            throw new Error(errorData.error || 'Failed to update order status');
        }

        showAlert('Order status updated successfully!', 'success');
        loadOrders(); // Refresh the table

    } catch (error) {
        console.error('Error updating order status:', error);
        showAlert('Error updating order status: ' + error.message, 'error');
    }
}

async function updateOrderDiscount(orderType, orderId) {
    try {
        const response = await fetch(`/api/orders/${orderType}/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();

        const newDiscount = prompt(
            `Current Discount: ${order.discountPercentage || 0}%\nEnter new discount percentage (0-100):`,
            order.discountPercentage || 0
        );

        if (newDiscount === null) return;

        const discountPercentage = parseFloat(newDiscount);
        if (isNaN(discountPercentage) || discountPercentage < 0 || discountPercentage > 100) {
            showAlert('Please enter a valid discount percentage between 0 and 100', 'error');
            return;
        }

        const updateResponse = await fetch(`/api/orders/${orderType}/${orderId}/discount?discountPercentage=${discountPercentage}&updatedBy=SalesManager`, {
            method: 'PUT'
        });

        if (!updateResponse.ok) {
            const errorData = await updateResponse.json();
            throw new Error(errorData.error || 'Failed to update order discount');
        }

        showAlert('Order discount updated successfully!', 'success');
        loadOrders(); // Refresh the table

    } catch (error) {
        console.error('Error updating order discount:', error);
        showAlert('Error updating order discount: ' + error.message, 'error');
    }
}

// Summary Report
async function loadSummaryReport() {
    try {
        const response = await fetch('/api/orders');
        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();

        const totalSales = orders.reduce((sum, order) => sum + order.totalAmount, 0);
        const ordersCount = orders.length;
        const customersCount = new Set(orders.map(order => order.customerEmail)).size;
        const successRate = ordersCount > 0 ? Math.round((orders.filter(order => order.status === 'READY_TO_SHIP').length / ordersCount) * 100) : 0;

        document.getElementById('totalSales').textContent = '$' + totalSales.toLocaleString();
        document.getElementById('ordersCount').textContent = ordersCount;
        document.getElementById('customersCount').textContent = customersCount;
        document.getElementById('successRateReport').textContent = successRate + '%';

        initializeReportChart(orders);
        loadTopProducts(orders);

    } catch (error) {
        console.error('Error loading summary report:', error);
    }
}

function initializeReportChart(orders) {
    const ctx = document.getElementById('reportChart')?.getContext('2d');
    if (!ctx) return;

    // Group orders by month for chart data
    const monthlySales = {};
    orders.forEach(order => {
        const month = new Date(order.placeDate).toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
        if (!monthlySales[month]) {
            monthlySales[month] = 0;
        }
        monthlySales[month] += order.totalAmount;
    });

    const labels = Object.keys(monthlySales);
    const data = Object.values(monthlySales);

    const reportChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Sales ($)',
                data: data,
                backgroundColor: 'rgba(76, 175, 80, 0.8)',
                borderColor: 'rgba(76, 175, 80, 1)',
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

function loadTopProducts(orders) {
    const products = {};
    orders.forEach(order => {
        if (!products[order.productType]) {
            products[order.productType] = { sales: 0, units: 0 };
        }
        products[order.productType].sales += order.totalAmount;
        products[order.productType].units += order.quantity;
    });

    const productsList = document.querySelector('.products-list');
    if (!productsList) return;

    productsList.innerHTML = '';

    Object.entries(products).forEach(([productName, data]) => {
        const productItem = document.createElement('div');
        productItem.className = 'product-item';
        productItem.innerHTML = `
            <div>
                <strong>${productName}</strong>
                <div style="font-size: 12px; color: #666;">${data.units} units sold</div>
            </div>
            <div style="font-weight: bold; color: #4caf50;">$${data.sales.toLocaleString()}</div>
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
        const response = await fetch('/api/buyers');
        if (!response.ok) throw new Error('Failed to load customers');

        const customers = await response.json();

        const customerSelect = document.getElementById('customerSelect');
        if (!customerSelect) return;

        customerSelect.innerHTML = '<option value="">Select Customer</option>';

        customers.forEach(customer => {
            const option = document.createElement('option');
            option.value = customer.email;
            option.textContent = `${customer.customerName} (${customer.email})`;
            customerSelect.appendChild(option);
        });

    } catch (error) {
        console.error('Error loading customers:', error);
    }
}

async function loadMessageHistory() {
    // Simulate loading message history
    console.log('Loading message history...');
}

function refreshMessagesTable(messages) {
    // Implementation for message history table
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

        // Show edit form
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
    if (header && header.parentNode) {
        header.parentNode.insertBefore(alertDiv, header.nextSibling);
    }

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
window.updateOrderStatus = updateOrderStatus;
window.updateOrderDiscount = updateOrderDiscount;
window.updateReport = updateReport;
window.generateReport = generateReport;
window.sendMessage = sendMessage;
window.resetMessageForm = resetMessageForm;
window.calculateTotal = calculateTotal;

// Export customer management functions
window.createCustomer = createCustomer;
window.loadBuyersForSelection = loadBuyersForSelection;
window.loadCustomerData = loadCustomerData;
window.loadAllBuyers = loadAllBuyers;
window.editCustomer = editCustomer;
window.deleteCustomer = deleteCustomer;
window.filterCustomers = filterCustomers;
window.clearCustomerForm = clearCustomerForm;