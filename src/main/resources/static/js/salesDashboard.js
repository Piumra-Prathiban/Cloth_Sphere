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
        loadProducts();
    } else if (sectionId === 'profile') {
        // Refresh quick stats when viewing profile/dashboard
        loadQuickStats();
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

    // Start auto-refresh for stats
    startStatsAutoRefresh();

    // Add real-time search listeners
    setupRealTimeSearch();

    // Test product loading
    setTimeout(debugProductLoading, 1000);
});

// Setup real-time search functionality
function setupRealTimeSearch() {
    // Order search
    const orderSearch = document.getElementById('orderSearch');
    if (orderSearch) {
        orderSearch.addEventListener('input', filterOrders);
    }

    // Order status filter
    const orderStatusFilter = document.getElementById('orderStatusFilter');
    if (orderStatusFilter) {
        orderStatusFilter.addEventListener('change', filterOrders);
    }

    // Customer search
    const customerSearch = document.getElementById('customerSearch');
    if (customerSearch) {
        customerSearch.addEventListener('input', filterCustomers);
    }
}

// Debug product loading
async function debugProductLoading() {
    try {
        console.log('Testing product API endpoint...');
        const response = await fetch('/api/products');
        console.log('API Response status:', response.status);

        if (response.ok) {
            const products = await response.json();
            console.log('Products received:', products);
            console.log('Number of products:', products.length);

            if (products.length > 0) {
                console.log('First product sample:', products[0]);
            }
        } else {
            console.error('API returned error status:', response.status);
        }
    } catch (error) {
        console.error('Debug - Error fetching products:', error);
    }
}

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

// Enhanced Quick Stats with real-time updates
async function loadQuickStats() {
    try {
        const response = await fetch('/api/orders');
        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();
        const stats = getOrderStatusCounts(orders);

        // Update with animation
        updateStatWithAnimation('totalOrders', stats.total);
        updateStatWithAnimation('pendingOrders', stats.pending);
        updateStatWithAnimation('completedOrders', stats.readyToShip); // READY_TO_SHIP = completed

        const successRate = stats.total > 0 ? Math.round((stats.readyToShip / stats.total) * 100) : 0;
        updateStatWithAnimation('successRate', successRate + '%');

    } catch (error) {
        console.error('Error loading quick stats:', error);
        // Set default values on error
        updateStatWithAnimation('totalOrders', 0);
        updateStatWithAnimation('pendingOrders', 0);
        updateStatWithAnimation('completedOrders', 0);
        updateStatWithAnimation('successRate', '0%');
    }
}

// Helper function to update stats with animation
function updateStatWithAnimation(elementId, newValue) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = newValue;
        element.parentElement.classList.add('stat-updated');
        setTimeout(() => {
            element.parentElement.classList.remove('stat-updated');
        }, 500);
    }
}

// Get detailed order status counts
function getOrderStatusCounts(orders) {
    const statusCounts = {
        total: orders.length,
        pending: 0,
        inProduction: 0,
        readyToShip: 0,
        other: 0
    };

    orders.forEach(order => {
        switch (order.status) {
            case 'PENDING':
                statusCounts.pending++;
                break;
            case 'IN_PRODUCTION':
                statusCounts.inProduction++;
                break;
            case 'READY_TO_SHIP':
                statusCounts.readyToShip++;
                break;
            default:
                statusCounts.other++;
        }
    });

    return statusCounts;
}

// Auto-refresh stats every 30 seconds when on dashboard
function startStatsAutoRefresh() {
    setInterval(() => {
        if (document.getElementById('profile').classList.contains('active')) {
            loadQuickStats();
        }
    }, 30000); // Refresh every 30 seconds
}

// Update stats when orders change
function updateQuickStatsAfterOrderChange() {
    loadQuickStats();

    // Also update summary report if it's currently active
    if (document.getElementById('summaryReport').classList.contains('active')) {
        loadSummaryReport();
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
        console.log('Loaded products from API:', products); // Debug log
        populateProductDropdown(products);

    } catch (error) {
        console.error('Error loading products:', error);
        showAlert('Error loading products: ' + error.message, 'error');
        // Fallback to static products if API fails
        loadFallbackProducts();
    }
}

// Populate product type dropdown with actual products
function populateProductDropdown(products) {
    const productTypeSelect = document.getElementById('productType');
    if (!productTypeSelect) {
        console.error('Product type select element not found');
        return;
    }

    // Clear existing options except the first one
    productTypeSelect.innerHTML = '<option value="">Select Product Type</option>';

    if (!products || products.length === 0) {
        console.warn('No products available');
        const option = document.createElement('option');
        option.value = '';
        option.textContent = 'No products available';
        productTypeSelect.appendChild(option);
        return;
    }

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
            option.value = product.productId || product.name; // Use productId as value
            option.textContent = `${product.name} - $${product.price} (Code: ${product.code})`;
            option.setAttribute('data-product', JSON.stringify(product));
            optgroup.appendChild(option);
        });

        productTypeSelect.appendChild(optgroup);
    });

    console.log('Product dropdown populated with', products.length, 'products');
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

// Setup product selection handler
function setupProductSelection() {
    const productTypeSelect = document.getElementById('productType');
    if (productTypeSelect) {
        productTypeSelect.addEventListener('change', function() {
            const selectedOption = this.options[this.selectedIndex];
            if (selectedOption.value && selectedOption.getAttribute('data-product')) {
                try {
                    const product = JSON.parse(selectedOption.getAttribute('data-product'));
                    autoFillProductDetails(product);
                } catch (e) {
                    console.error('Error parsing product data:', e);
                    showAlert('Error loading product details', 'error');
                }
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

        // Format status for display and filtering
        const statusDisplay = order.status.replace('_', ' ');
        const statusClass = `status-${order.status.toLowerCase().replace('_', '-')}`;

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
            <td><span class="status-badge ${statusClass}" data-status="${order.status}">${statusDisplay}</span></td>
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

    // Apply current filters after loading
    setTimeout(() => filterOrders(), 100);
}

function filterOrders() {
    const statusFilter = document.getElementById('orderStatusFilter').value;
    const searchTerm = document.getElementById('orderSearch').value.toLowerCase().trim();

    const rows = document.querySelectorAll('#ordersTable tbody tr');
    let visibleCount = 0;

    rows.forEach(row => {
        if (row.classList.contains('no-results')) {
            row.style.display = 'none';
            return;
        }

        const statusBadge = row.querySelector('.status-badge');
        const customerNameCell = row.querySelector('td:nth-child(3)');
        const orderIdCell = row.querySelector('td:nth-child(1)');
        const productTypeCell = row.querySelector('td:nth-child(4)');

        const status = statusBadge?.getAttribute('data-status') || '';
        const customerName = customerNameCell?.textContent?.toLowerCase() || '';
        const orderId = orderIdCell?.textContent?.toLowerCase() || '';
        const productType = productTypeCell?.textContent?.toLowerCase() || '';

        // Improved status matching
        let statusMatch = false;
        if (statusFilter === 'ALL') {
            statusMatch = true;
        } else if (statusFilter === 'PENDING' && status === 'PENDING') {
            statusMatch = true;
        } else if (statusFilter === 'IN_PRODUCTION' && status === 'IN_PRODUCTION') {
            statusMatch = true;
        } else if (statusFilter === 'READY_TO_SHIP' && status === 'READY_TO_SHIP') {
            statusMatch = true;
        }

        const searchMatch = !searchTerm ||
            customerName.includes(searchTerm) ||
            orderId.includes(searchTerm) ||
            productType.includes(searchTerm);

        const shouldShow = statusMatch && searchMatch;
        row.style.display = shouldShow ? '' : 'none';

        if (shouldShow) visibleCount++;
    });

    // Handle no results message
    const tbody = document.querySelector('#ordersTable tbody');
    let noResultsRow = tbody.querySelector('.no-results');

    if (visibleCount === 0) {
        if (!noResultsRow) {
            noResultsRow = document.createElement('tr');
            noResultsRow.className = 'no-results';
            noResultsRow.innerHTML = `<td colspan="11" style="text-align: center; color: #666; padding: 20px; font-style: italic;">No orders found matching your criteria</td>`;
            tbody.appendChild(noResultsRow);
        }
        noResultsRow.style.display = '';
    } else if (noResultsRow) {
        noResultsRow.style.display = 'none';
    }
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

        // Refresh quick stats - UPDATED
        updateQuickStatsAfterOrderChange();

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

// Modal state variables
let currentOrderType = '';
let currentOrderId = '';
let currentCustomerId = '';
let currentCustomerEmail = '';

// Order View Modal Functions
async function viewOrder(orderType, orderId) {
    try {
        const response = await fetch(`/api/orders/${orderType}/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();
        showOrderViewModal(order);

    } catch (error) {
        console.error('Error viewing order:', error);
        showAlert('Error loading order details', 'error');
    }
}

function showOrderViewModal(order) {
    // Populate modal with order data
    document.getElementById('modalOrderId').textContent = order.orderId;
    document.getElementById('modalOrderType').textContent = order.orderType;
    document.getElementById('modalCustomerName').textContent = order.customerName;
    document.getElementById('modalCustomerEmail').textContent = order.customerEmail || 'N/A';
    document.getElementById('modalCustomerPhone').textContent = order.customerPhone || 'N/A';
    document.getElementById('modalCustomerAddress').textContent = order.customerAddress || 'N/A';
    document.getElementById('modalProductType').textContent = order.productType;
    document.getElementById('modalQuantity').textContent = order.quantity;
    document.getElementById('modalUnitPrice').textContent = `$${order.unitPrice.toFixed(2)}`;
    document.getElementById('modalDiscount').textContent = `${order.discountPercentage || 0}%`;
    document.getElementById('modalTotalAmount').textContent = `$${order.totalAmount.toFixed(2)}`;
    document.getElementById('modalStatus').textContent = order.status;
    document.getElementById('modalOrderNotes').textContent = order.orderNotes || 'No notes';
    document.getElementById('modalOrderDate').textContent = new Date(order.placeDate).toLocaleDateString();
    document.getElementById('modalCreatedBy').textContent = order.createdBy || 'N/A';

    // Show modal
    document.getElementById('orderViewModal').style.display = 'block';
}

function closeOrderViewModal() {
    document.getElementById('orderViewModal').style.display = 'none';
}

// Update Status Modal Functions
async function updateOrderStatus(orderType, orderId) {
    try {
        currentOrderType = orderType;
        currentOrderId = orderId;

        const response = await fetch(`/api/orders/${orderType}/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();
        showUpdateStatusModal(order);

    } catch (error) {
        console.error('Error loading order details:', error);
        showAlert('Error loading order details', 'error');
    }
}

function showUpdateStatusModal(order) {
    // Populate current status
    document.getElementById('currentStatusDisplay').textContent = order.status;

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

    document.getElementById('availableStatusesDisplay').textContent = availableStatuses.join(', ');

    // Populate status dropdown
    const statusSelect = document.getElementById('newStatusSelect');
    statusSelect.innerHTML = '<option value="">Select Status</option>';

    availableStatuses.forEach(status => {
        const option = document.createElement('option');
        option.value = status;
        option.textContent = status.replace('_', ' ');
        statusSelect.appendChild(option);
    });

    // Show modal
    document.getElementById('updateStatusModal').style.display = 'block';
}

function closeUpdateStatusModal() {
    document.getElementById('updateStatusModal').style.display = 'none';
    currentOrderType = '';
    currentOrderId = '';
}

async function confirmStatusUpdate() {
    const newStatus = document.getElementById('newStatusSelect').value;

    if (!newStatus) {
        showAlert('Please select a status', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/orders/${currentOrderType}/${currentOrderId}/status?newStatus=${newStatus}&updatedBy=SalesManager`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to update order status');
        }

        const updatedOrder = await response.json();
        showAlert('Order status updated successfully!', 'success');
        closeUpdateStatusModal();
        loadOrders(); // Refresh the table

        // Refresh quick stats - UPDATED
        updateQuickStatsAfterOrderChange();

    } catch (error) {
        console.error('Error updating order status:', error);
        showAlert('Error updating order status: ' + error.message, 'error');
    }
}

// Update Discount Modal Functions
async function updateOrderDiscount(orderType, orderId) {
    try {
        currentOrderType = orderType;
        currentOrderId = orderId;

        const response = await fetch(`/api/orders/${orderType}/${orderId}`);
        if (!response.ok) throw new Error('Failed to load order details');

        const order = await response.json();
        showUpdateDiscountModal(order);

    } catch (error) {
        console.error('Error loading order details:', error);
        showAlert('Error loading order details', 'error');
    }
}

function showUpdateDiscountModal(order) {
    // Populate current discount
    document.getElementById('currentDiscountDisplay').textContent = `${order.discountPercentage || 0}%`;
    document.getElementById('newDiscountInput').value = order.discountPercentage || 0;

    // Populate preview
    document.getElementById('previewQuantity').textContent = order.quantity;
    document.getElementById('previewUnitPrice').textContent = `$${order.unitPrice.toFixed(2)}`;
    updateDiscountPreview(order);

    // Add event listener for real-time preview
    document.getElementById('newDiscountInput').addEventListener('input', function() {
        updateDiscountPreview(order);
    });

    // Show modal
    document.getElementById('updateDiscountModal').style.display = 'block';
}

function updateDiscountPreview(order) {
    const discountPercentage = parseFloat(document.getElementById('newDiscountInput').value) || 0;
    const subtotal = order.quantity * order.unitPrice;
    const discountAmount = subtotal * (discountPercentage / 100);
    const total = subtotal - discountAmount;

    document.getElementById('previewTotalAmount').textContent = `$${total.toFixed(2)}`;
}

function closeUpdateDiscountModal() {
    document.getElementById('updateDiscountModal').style.display = 'none';
    currentOrderType = '';
    currentOrderId = '';
}

async function confirmDiscountUpdate() {
    const discountPercentage = parseFloat(document.getElementById('newDiscountInput').value);

    if (isNaN(discountPercentage) || discountPercentage < 0 || discountPercentage > 100) {
        showAlert('Please enter a valid discount percentage between 0 and 100', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/orders/${currentOrderType}/${currentOrderId}/discount?discountPercentage=${discountPercentage}&updatedBy=SalesManager`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to update order discount');
        }

        const updatedOrder = await response.json();
        showAlert('Order discount updated successfully!', 'success');
        closeUpdateDiscountModal();
        loadOrders(); // Refresh the table

        // Refresh quick stats - UPDATED
        updateQuickStatsAfterOrderChange();

    } catch (error) {
        console.error('Error updating order discount:', error);
        showAlert('Error updating order discount: ' + error.message, 'error');
    }
}

// Customer Edit Modal Functions
async function editCustomer(buyerId, email) {
    try {
        currentCustomerId = buyerId;
        currentCustomerEmail = email;

        const response = await fetch(`/api/buyers/${buyerId}/${encodeURIComponent(email)}`);
        if (!response.ok) throw new Error('Failed to load customer details');

        const customer = await response.json();
        showCustomerEditModal(customer);

    } catch (error) {
        console.error('Error loading customer details:', error);
        showAlert('Error loading customer details', 'error');
    }
}

function showCustomerEditModal(customer) {
    // Populate form with customer data
    document.getElementById('editCustomerName').value = customer.customerName;
    document.getElementById('editCustomerEmail').value = customer.email;
    document.getElementById('editCustomerPhone').value = customer.phone || '';
    document.getElementById('editCustomerAddress').value = customer.address || '';
    document.getElementById('editCustomerCompany').value = customer.company || '';

    // Make email field read-only
    document.getElementById('editCustomerEmail').readOnly = true;
    document.getElementById('editCustomerEmail').style.backgroundColor = '#f8f9fa';
    document.getElementById('editCustomerEmail').title = 'Email cannot be changed';

    // Show modal
    document.getElementById('customerEditModal').style.display = 'block';
}

function closeCustomerEditModal() {
    document.getElementById('customerEditModal').style.display = 'none';
    currentCustomerId = '';
    currentCustomerEmail = '';
}

async function confirmCustomerEdit() {
    const formData = {
        customerName: document.getElementById('editCustomerName').value,
        email: document.getElementById('editCustomerEmail').value,
        phone: document.getElementById('editCustomerPhone').value,
        address: document.getElementById('editCustomerAddress').value,
        company: document.getElementById('editCustomerCompany').value
    };

    // Validation
    if (!formData.customerName || !formData.email || !formData.phone || !formData.address) {
        showAlert('Please fill in all required fields', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/buyers/${currentCustomerId}/${encodeURIComponent(currentCustomerEmail)}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to update customer');
        }

        const updatedCustomer = await response.json();
        showAlert('Customer updated successfully!', 'success');
        closeCustomerEditModal();
        loadAllBuyers(); // Refresh the table

    } catch (error) {
        console.error('Error updating customer:', error);
        showAlert('Error updating customer: ' + error.message, 'error');
    }
}

// Customer Delete Modal Functions
async function deleteCustomer(buyerId, email) {
    currentCustomerId = buyerId;
    currentCustomerEmail = email;

    try {
        const response = await fetch(`/api/buyers/${buyerId}/${encodeURIComponent(email)}`);
        if (!response.ok) throw new Error('Failed to load customer details');

        const customer = await response.json();
        showCustomerDeleteModal(customer);

    } catch (error) {
        console.error('Error loading customer details:', error);
        showAlert('Error loading customer details', 'error');
    }
}

function showCustomerDeleteModal(customer) {
    // Populate confirmation data
    document.getElementById('deleteCustomerId').textContent = customer.buyerId;
    document.getElementById('deleteCustomerName').textContent = customer.customerName;
    document.getElementById('deleteCustomerEmail').textContent = customer.email;

    // Show modal
    document.getElementById('customerDeleteModal').style.display = 'block';
}

function closeCustomerDeleteModal() {
    document.getElementById('customerDeleteModal').style.display = 'none';
    currentCustomerId = '';
    currentCustomerEmail = '';
}

async function confirmCustomerDelete() {
    try {
        const response = await fetch(`/api/buyers/${currentCustomerId}/${encodeURIComponent(currentCustomerEmail)}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to delete customer');
        }

        showAlert('Customer deleted successfully!', 'success');
        closeCustomerDeleteModal();
        loadAllBuyers(); // Refresh the table

        // Also refresh the customer dropdown in create order form
        loadBuyersForSelection();

    } catch (error) {
        console.error('Error deleting customer:', error);
        showAlert('Error deleting customer: ' + error.message, 'error');
    }
}

// Close modals when clicking outside
window.onclick = function(event) {
    const modals = document.getElementsByClassName('modal');
    for (let modal of modals) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
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
        // Set default values on error
        document.getElementById('totalSales').textContent = '$0';
        document.getElementById('ordersCount').textContent = '0';
        document.getElementById('customersCount').textContent = '0';
        document.getElementById('successRateReport').textContent = '0%';
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

        // Enhanced Validation
        if (!customerData.customerName || !customerData.email || !customerData.phone || !customerData.address) {
            showAlert('Please fill in all required customer fields', 'error');
            return;
        }

        // Email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(customerData.email)) {
            showAlert('Please enter a valid email address with @ symbol', 'error');
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

function filterCustomers() {
    const searchTerm = document.getElementById('customerSearch').value.toLowerCase().trim();
    const rows = document.querySelectorAll('#customersTable tbody tr');
    let visibleCount = 0;

    rows.forEach(row => {
        const customerName = row.cells[1]?.textContent?.toLowerCase() || '';
        const email = row.cells[2]?.textContent?.toLowerCase() || '';
        const phone = row.cells[3]?.textContent?.toLowerCase() || '';
        const company = row.cells[4]?.textContent?.toLowerCase() || '';

        const matchesSearch = !searchTerm ||
            customerName.includes(searchTerm) ||
            email.includes(searchTerm) ||
            phone.includes(searchTerm) ||
            company.includes(searchTerm);

        row.style.display = matchesSearch ? '' : 'none';
        if (matchesSearch) visibleCount++;
    });

    // Show message if no customers found
    const tbody = document.querySelector('#customersTable tbody');
    const noResultsRow = tbody.querySelector('.no-results');

    if (visibleCount === 0) {
        if (!noResultsRow) {
            const row = document.createElement('tr');
            row.className = 'no-results';
            row.innerHTML = `<td colspan="7" style="text-align: center; color: #666;">No customers found matching your search</td>`;
            tbody.appendChild(row);
        }
    } else if (noResultsRow) {
        noResultsRow.remove();
    }
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

// Export modal functions
window.closeOrderViewModal = closeOrderViewModal;
window.closeUpdateStatusModal = closeUpdateStatusModal;
window.confirmStatusUpdate = confirmStatusUpdate;
window.closeUpdateDiscountModal = closeUpdateDiscountModal;
window.confirmDiscountUpdate = confirmDiscountUpdate;
window.closeCustomerEditModal = closeCustomerEditModal;
window.confirmCustomerEdit = confirmCustomerEdit;
window.closeCustomerDeleteModal = closeCustomerDeleteModal;
window.confirmCustomerDelete = confirmCustomerDelete;