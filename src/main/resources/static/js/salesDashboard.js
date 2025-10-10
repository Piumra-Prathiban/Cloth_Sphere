// Navigation functionality
function showSection(sectionId, event) {
    // Prevent default if event is provided
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    console.log('Switching to section:', sectionId);

    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    } else {
        console.error('Section not found:', sectionId);
        return;
    }

    // Update navigation links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });

    // Find and activate the clicked nav link
    if (event && event.target) {
        // Handle case where click might be on the icon instead of the link
        let targetElement = event.target;
        if (targetElement.tagName === 'I') {
            targetElement = targetElement.parentElement;
        }
        if (targetElement.tagName === 'A') {
            targetElement.classList.add('active');
        }
    }

    // Refresh data when switching to specific sections
    if (sectionId === 'orderView') {
        console.log('Loading orders...');
        loadOrders();
    } else if (sectionId === 'summaryReport') {
        console.log('Initializing summary report...');
        initializeSummaryReport();
    } else if (sectionId === 'customerMessage') {
        console.log('Loading customer messages...');
        loadCustomers();
        loadMessageHistory();
    } else if (sectionId === 'customerManagement') {
        console.log('Loading all buyers...');
        loadAllBuyers();
    } else if (sectionId === 'createOrder') {
        console.log('Loading create order data...');
        loadBuyersForSelection();
        loadProducts();
    } else if (sectionId === 'profile') {
        console.log('Refreshing quick stats...');
        loadQuickStats();
    }
}

// Update the DOMContentLoaded function
document.addEventListener('DOMContentLoaded', function() {
    console.log('Sales Dashboard initialized');

    // Initialize sales chart
    initializeSalesChart();

    // Load initial data
    loadQuickStats();

    // Set up form event listeners
    setupFormListeners();

    // Setup quantity validation
    setupQuantityValidation();

    // Load products with proper sequencing
    loadProducts().then((products) => {
        console.log('Products loaded successfully, count:', products.length);
        setTimeout(() => {
            setupProductSelection();
            console.log('Product selection setup complete');
        }, 200);
    }).catch(error => {
        console.error('Failed to load products:', error);
        setTimeout(() => {
            setupProductSelection();
        }, 200);
    });

    // Start auto-refresh for stats
    startStatsAutoRefresh();

    // Start auto-refresh for summary reports
    startSummaryAutoRefresh();

    // Add real-time search listeners
    setupRealTimeSearch();

    console.log('Dashboard initialization complete');
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


// Form setup - FIXED VERSION
function setupFormListeners() {
    // Order form submission
    const orderForm = document.getElementById('orderForm');
    if (orderForm) {
        orderForm.addEventListener('submit', function(e) {
            e.preventDefault();
            if (validateOrderForm()) {
                createOrder();
            }
        });
    }

    // Message form submission (only if it exists)
    const messageForm = document.getElementById('messageForm');
    if (messageForm) {
        messageForm.addEventListener('submit', function(e) {
            e.preventDefault();
            sendMessage();
        });
    }

    // Add event listeners for real-time calculation
    const quantityInput = document.getElementById('quantity');
    const discountInput = document.getElementById('discountPercentage');

    if (quantityInput) {
        quantityInput.addEventListener('input', calculateTotal);
    }
    if (discountInput) {
        discountInput.addEventListener('input', calculateTotal);
    }
}

// Enhanced product loading with better error handling
async function loadProducts() {
    try {
        console.log('Loading products from API...');
        const response = await fetch('/api/products');

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const products = await response.json();
        console.log('Products loaded successfully:', products);

        // Validate and transform product data
        const validProducts = products.filter(product =>
            product && product.name && (product.price !== undefined && product.price !== null)
        ).map(product => ({
            name: product.name,
            price: parseFloat(product.price) || 0,
            code: product.code || 'N/A',
            category: product.category || 'General',
            productId: product.productId || product.id?.toString(),
            stock: product.stock || 0
        }));

        if (validProducts.length === 0) {
            console.warn('No valid products found');
            useFallbackProducts();
            return;
        }

        populateProductDropdown(validProducts);
        return validProducts;

    } catch (error) {
        console.error('Error loading products:', error);
        useFallbackProducts();
    }
}

function useFallbackProducts() {
    const fallbackProducts = [
        {
            name: "Classic Cotton T-Shirt",
            price: 19.99,
            code: "TS001",
            category: "Shirts",
            productId: "P01"
        },
        {
            name: "Premium V-Neck Tee",
            price: 24.99,
            code: "TS002",
            category: "Shirts",
            productId: "P02"
        },
        {
            name: "Designer Polo Shirt",
            price: 34.99,
            code: "PL001",
            category: "Shirts",
            productId: "P03"
        }
    ];

    populateProductDropdown(fallbackProducts);
}
// Enhanced product loading
function populateProductDropdown(products) {
    const productTypeSelect = document.getElementById('productType');
    if (!productTypeSelect) return;

    productTypeSelect.innerHTML = '<option value="">Select Product Type</option>';

    products.forEach(product => {
        const option = document.createElement('option');
        option.value = product.name;

        // Ensure all product data is available
        const productData = {
            name: product.name,
            price: product.price || 0,
            code: product.code || 'N/A',
            category: product.category || 'General',
            productId: product.productId || product.id?.toString() || 'UNKNOWN',
            stock: product.stock || 0
        };

        option.textContent = `${product.name} - $${productData.price.toFixed(2)} (Stock: ${productData.stock})`;
        option.setAttribute('data-product', JSON.stringify(productData));

        // Store individual attributes as backup
        option.setAttribute('data-product-id', productData.productId);
        option.setAttribute('data-price', productData.price);
        option.setAttribute('data-stock', productData.stock);
        option.setAttribute('data-code', productData.code);

        if (productData.stock <= 0) {
            option.disabled = true;
            option.textContent += ' - OUT OF STOCK';
        }

        productTypeSelect.appendChild(option);
    });
}

// Enhanced quantity input with stock validation
function setupQuantityValidation() {
    const quantityInput = document.getElementById('quantity');
    if (quantityInput) {
        quantityInput.addEventListener('change', validateQuantityAgainstStock);
        quantityInput.addEventListener('input', validateQuantityAgainstStock);
    }
}

// Validate quantity against available stock
async function validateQuantityAgainstStock() {
    const productTypeSelect = document.getElementById('productType');
    const selectedOption = productTypeSelect.options[productTypeSelect.selectedIndex];
    const quantity = parseInt(this.value) || 0;

    if (!selectedOption.value || quantity <= 0) {
        return;
    }

    try {
        const productJson = selectedOption.getAttribute('data-product');
        if (productJson) {
            const productData = JSON.parse(productJson);
            const availableStock = productData.stock || 0;

            if (quantity > availableStock) {
                this.style.borderColor = '#dc3545';
                showAlert(`Warning: Only ${availableStock} units available in stock`, 'warning');
            } else {
                this.style.borderColor = '';
                // Clear any existing warning alerts
                const existingAlerts = document.querySelectorAll('.alert');
                existingAlerts.forEach(alert => {
                    if (alert.textContent.includes('Warning:')) {
                        alert.remove();
                    }
                });
            }
        }
    } catch (error) {
        console.error('Error validating quantity:', error);
    }
}

// Improved product selection handler
function setupProductSelection() {
    const productTypeSelect = document.getElementById('productType');
    if (!productTypeSelect) {
        console.error('Product type select element not found');
        return;
    }

    // Remove existing event listeners
    const newSelect = productTypeSelect.cloneNode(true);
    productTypeSelect.parentNode.replaceChild(newSelect, productTypeSelect);

    // Add new event listener
    document.getElementById('productType').addEventListener('change', function() {
        handleProductSelection(this);
    });
}


function handleProductSelection(selectElement) {
    const selectedOption = selectElement.options[selectElement.selectedIndex];

    if (!selectedOption.value) {
        clearProductDetails();
        return;
    }

    try {
        let productData;
        const productJson = selectedOption.getAttribute('data-product');

        if (productJson) {
            productData = JSON.parse(productJson);
        } else {
            // Fallback: get data from individual attributes
            productData = {
                name: selectedOption.getAttribute('data-name') || selectedOption.textContent.split(' - ')[0],
                price: parseFloat(selectedOption.getAttribute('data-price')) || 0,
                code: selectedOption.getAttribute('data-code') || 'N/A',
                productId: selectedOption.getAttribute('data-product-id') || '',
                stock: parseInt(selectedOption.getAttribute('data-stock')) || 0
            };
        }

        if (productData && productData.price > 0) {
            autoFillProductDetails(productData);
        } else {
            showAlert('Invalid product price. Please select another product.', 'error');
            selectElement.selectedIndex = 0;
            clearProductDetails();
        }

    } catch (error) {
        console.error('Error handling product selection:', error);
        showAlert('Error loading product details.', 'error');
        selectElement.selectedIndex = 0;
        clearProductDetails();
    }
}

// Extract product info from option text
function extractProductFromText(text) {
    console.log('Extracting product from text:', text);

    // Pattern: "Product Name - $Price (Code: CODE)"
    const pattern = /(.+?)\s*-\s*\$\s*(\d+\.?\d*)\s*\(Code:\s*([^)]+)\)/;
    const match = text.match(pattern);

    if (match) {
        return {
            name: match[1].trim(),
            price: parseFloat(match[2]),
            code: match[3].trim()
        };
    }

    // Alternative pattern: "Product Name - Price (Code: CODE)"
    const altPattern = /(.+?)\s*-\s*(\d+\.?\d*)\s*\(Code:\s*([^)]+)\)/;
    const altMatch = text.match(altPattern);

    if (altMatch) {
        return {
            name: altMatch[1].trim(),
            price: parseFloat(altMatch[2]),
            code: altMatch[3].trim()
        };
    }

    return null;
}

// Clear product details function
function clearProductDetails() {
    const unitPriceInput = document.getElementById('unitPrice');
    if (unitPriceInput) {
        unitPriceInput.removeAttribute('readonly');
        unitPriceInput.value = '';
        unitPriceInput.setAttribute('readonly', 'readonly');
    }

    const subtotalInput = document.getElementById('subtotalAmount');
    if (subtotalInput) subtotalInput.value = '';

    const totalInput = document.getElementById('totalAmount');
    if (totalInput) totalInput.value = '';

    console.log('Product details cleared');
}

// Autofill product details - ENHANCED VERSION
function autoFillProductDetails(product) {
    console.log('Auto-filling product details with:', product);

    const unitPriceInput = document.getElementById('unitPrice');
    const quantityInput = document.getElementById('quantity');

    if (!unitPriceInput) {
        console.error('Unit price input not found!');
        return;
    }

    // Get the price - handle different field names
    let price = product.price || product.unitPrice || product.cost || 0;

    // Ensure price is a number
    if (typeof price === 'string') {
        price = parseFloat(price);
    }

    console.log('Final price to set:', price);

    if (isNaN(price) || price <= 0) {
        console.error('Invalid price:', price);
        showAlert('Invalid product price. Please select another product.', 'error');
        return;
    }

    // Update unit price field
    unitPriceInput.removeAttribute('readonly');
    unitPriceInput.value = price.toFixed(2);
    unitPriceInput.setAttribute('readonly', 'readonly');

    console.log('Unit price set to:', unitPriceInput.value);

    // Ensure quantity has a value
    if (!quantityInput.value || quantityInput.value === '0' || quantityInput.value === '') {
        quantityInput.value = '1';
    }

    // Calculate totals
    calculateTotal();

    // Show success message
    const productName = product.name || 'Product';
    showAlert(`Product "${productName}" selected - Price: $${price.toFixed(2)}`, 'success');
}

// Calculate order total with discount - ROBUST VERSION
function calculateTotal() {
    const quantity = parseInt(document.getElementById('quantity').value) || 0;
    const unitPrice = parseFloat(document.getElementById('unitPrice').value) || 0;
    const discountPercentage = parseFloat(document.getElementById('discountPercentage').value) || 0;

    console.log('Calculation inputs:', { quantity, unitPrice, discountPercentage });

    // Validate inputs
    if (quantity <= 0 || unitPrice <= 0) {
        document.getElementById('subtotalAmount').value = '0.00';
        document.getElementById('totalAmount').value = '0.00';
        console.log('Invalid inputs, setting totals to 0');
        return;
    }

    const subtotal = quantity * unitPrice;
    const discountAmount = subtotal * (discountPercentage / 100);
    const total = Math.max(0, subtotal - discountAmount);

    document.getElementById('subtotalAmount').value = subtotal.toFixed(2);
    document.getElementById('totalAmount').value = total.toFixed(2);

    console.log('Calculated totals:', {
        subtotal: subtotal.toFixed(2),
        total: total.toFixed(2)
    });
}

// Add this function to validate the form before submission
function validateOrderForm() {
    const unitPrice = parseFloat(document.getElementById('unitPrice').value);
    const productType = document.getElementById('productType').value;

    if (!productType) {
        showAlert('Please select a product', 'error');
        return false;
    }

    if (isNaN(unitPrice) || unitPrice <= 0) {
        showAlert('Please select a valid product with a price', 'error');
        return false;
    }

    return true;
}

// Debug function to check form data before submission
function debugFormData() {
    const formData = new FormData(document.getElementById('orderForm'));
    const productTypeSelect = document.getElementById('productType');
    const selectedProduct = productTypeSelect.options[productTypeSelect.selectedIndex];

    console.log('=== FORM DEBUG INFO ===');
    console.log('Selected Product:', selectedProduct.textContent);
    console.log('Product Value:', selectedProduct.value);
    console.log('Product Data Attribute:', selectedProduct.getAttribute('data-product'));
    console.log('Unit Price Input Value:', document.getElementById('unitPrice').value);
    console.log('Unit Price Input Type:', typeof document.getElementById('unitPrice').value);
    console.log('Parsed Unit Price:', parseFloat(document.getElementById('unitPrice').value));
    console.log('========================');
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

// Fixed order creation function with proper product ID handling
async function createOrder() {
    try {
        const formData = new FormData(document.getElementById('orderForm'));
        const productTypeSelect = document.getElementById('productType');
        const selectedOption = productTypeSelect.options[productTypeSelect.selectedIndex];

        if (!selectedOption.value) {
            showAlert('Please select a product', 'error');
            return;
        }

        // Get product data safely
        let productData;
        try {
            const productJson = selectedOption.getAttribute('data-product');
            productData = productJson ? JSON.parse(productJson) : null;
        } catch (e) {
            console.error('Error parsing product data:', e);
            productData = null;
        }

        // If no product data, extract from option attributes
        if (!productData) {
            productData = {
                name: selectedOption.value,
                price: parseFloat(document.getElementById('unitPrice').value) || 0,
                code: selectedOption.getAttribute('data-code') || 'N/A',
                productId: selectedOption.getAttribute('data-product-id') || 'UNKNOWN',
                stock: parseInt(selectedOption.getAttribute('data-stock')) || 0
            };
        }

        // Validate required fields
        const customerName = formData.get('customerName');
        const customerEmail = formData.get('customerEmail');
        const customerPhone = formData.get('customerPhone');
        const quantity = parseInt(formData.get('quantity'));
        const unitPrice = parseFloat(document.getElementById('unitPrice').value);

        if (!customerName || !customerEmail || !customerPhone) {
            showAlert('Please fill in all required customer fields', 'error');
            return;
        }

        if (isNaN(quantity) || quantity <= 0) {
            showAlert('Please enter a valid quantity', 'error');
            return;
        }

        if (isNaN(unitPrice) || unitPrice <= 0) {
            showAlert('Please select a valid product with a price', 'error');
            return;
        }

        // Stock validation
        if (productData.stock !== undefined && productData.stock < quantity) {
            showAlert(`Insufficient stock! Only ${productData.stock} units available.`, 'error');
            return;
        }

        // Prepare order data with proper product ID
        const orderData = {
            customerName: customerName,
            customerEmail: customerEmail,
            customerPhone: customerPhone,
            customerAddress: formData.get('customerAddress'),
            productType: productData.name,
            quantity: quantity,
            unitPrice: unitPrice,
            discountPercentage: parseFloat(formData.get('discountPercentage')) || 0,
            totalAmount: parseFloat(document.getElementById('totalAmount').value) || 0,
            orderType: 'PHYSICAL',
            // CRITICAL: Make sure productId is included in the notes in this exact format
            orderNotes: `Product: ${productData.name}, Code: ${productData.code}, ProductId: ${productData.productId}, Category: ${productData.category}`,
            status: 'PENDING'
        };

        console.log('Sending order data:', orderData);

        const response = await fetch('/api/orders?createdBy=SalesManager', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(orderData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to create order');
        }

        const createdOrder = await response.json();
        showAlert(`Order created successfully! Order ID: ${createdOrder.orderId}`, 'success');

        // Reset form and refresh data
        resetOrderFormOnly();
        refreshAfterOrderCreation();

    } catch (error) {
        console.error('Error creating order:', error);
        showAlert('Error creating order: ' + error.message, 'error');
    }
}
// Refresh data after order creation
function refreshAfterOrderCreation() {
    // Refresh orders list if on order view
    if (document.getElementById('orderView').classList.contains('active')) {
        loadOrders();
    }

    // Refresh quick stats
    updateQuickStatsAfterOrderChange();

    // Reload products to get updated stock
    loadProducts();
}

// Reset only the order form (not the entire page)
function resetOrderFormOnly() {
    // Reset form fields
    document.getElementById('orderForm').reset();

    // Clear calculated fields
    document.getElementById('subtotalAmount').value = '';
    document.getElementById('totalAmount').value = '';

    // Clear customer selection but keep customer management data
    document.getElementById('existingCustomer').value = '';

    // Reset product selection
    const productTypeSelect = document.getElementById('productType');
    if (productTypeSelect) {
        productTypeSelect.selectedIndex = 0;
    }

    // Clear unit price
    const unitPriceInput = document.getElementById('unitPrice');
    if (unitPriceInput) {
        unitPriceInput.removeAttribute('readonly');
        unitPriceInput.value = '';
        unitPriceInput.setAttribute('readonly', 'readonly');
    }

    console.log('Order form reset successfully');
}

// Update Product Stock Function
async function updateProductStock(productId, soldQuantity) {
    try {
        const response = await fetch(`/api/products/productId/${productId}`);
        if (!response.ok) {
            throw new Error('Failed to fetch product for stock update');
        }

        const product = await response.json();
        const newStock = product.stock - soldQuantity;

        // Update product stock
        const updateResponse = await fetch(`/api/products/${product.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...product,
                stock: newStock
            })
        });

        if (!updateResponse.ok) {
            throw new Error('Failed to update product stock');
        }

        console.log(`Stock updated for ${product.name}: ${product.stock} -> ${newStock}`);

    } catch (error) {
        console.error('Error updating product stock:', error);
        // Don't show alert to user as order was already created
    }
}

// Stock Validation Function
async function validateStock(productId, requestedQuantity) {
    try {
        const response = await fetch(`/api/products/productId/${productId}`);
        if (!response.ok) {
            throw new Error('Failed to fetch product details');
        }

        const product = await response.json();
        return {
            valid: product.stock >= requestedQuantity,
            availableStock: product.stock,
            product: product
        };
    } catch (error) {
        console.error('Error validating stock:', error);
        return { valid: false, availableStock: 0 };
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

//==========================================================================\
// Summary Report Functions
let monthlyRevenueChart = null;

// Initialize summary report
function initializeSummaryReport() {
    console.log('Initializing summary report...');
    setupDefaultDates();
    initializeMonthlyChart();
    loadSummaryData();
    loadGeneratedReports();
}

// Setup default date range
function setupDefaultDates() {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30);

    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    if (startDateInput && endDateInput) {
        startDateInput.value = startDate.toISOString().split('T')[0];
        endDateInput.value = endDate.toISOString().split('T')[0];
    }
}

// Load summary data
async function loadSummaryData() {
    await loadQuickStatsForReport();
    await loadMonthlyRevenueData();
}

// Load quick stats for report section
async function loadQuickStatsForReport() {
    try {
        const startDate = document.getElementById('startDate')?.value;
        const endDate = document.getElementById('endDate')?.value;

        if (!startDate || !endDate) {
            console.log('Date range not set yet');
            return;
        }

        const response = await fetch('/api/orders');
        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();

        // Filter orders by date range
        const startDateTime = new Date(startDate + 'T00:00:00');
        const endDateTime = new Date(endDate + 'T23:59:59');

        const filteredOrders = orders.filter(order => {
            const orderDate = new Date(order.placeDate);
            return orderDate >= startDateTime && orderDate <= endDateTime;
        });

        // Calculate stats
        const totalOrders = filteredOrders.length;
        const totalRevenue = filteredOrders.reduce((sum, order) => sum + order.totalAmount, 0);
        const uniqueCustomers = new Set(filteredOrders.map(order => order.customerEmail)).size;
        const successfulOrders = filteredOrders.filter(order => order.status === 'READY_TO_SHIP').length;
        const successRate = totalOrders > 0 ? (successfulOrders / totalOrders) * 100 : 0;

        // Update UI
        updateReportStat('totalOrdersReport', totalOrders);
        updateReportStat('totalRevenueReport', '$' + totalRevenue.toLocaleString());
        updateReportStat('totalCustomersReport', uniqueCustomers);
        updateReportStat('successRateReport', successRate.toFixed(1) + '%');

    } catch (error) {
        console.error('Error loading quick stats for report:', error);
        updateReportStat('totalOrdersReport', 0);
        updateReportStat('totalRevenueReport', '$0');
        updateReportStat('totalCustomersReport', 0);
        updateReportStat('successRateReport', '0%');
    }
}

// Update report stats
function updateReportStat(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
    }
}

// Load monthly revenue data for chart
async function loadMonthlyRevenueData() {
    try {
        const response = await fetch('/api/orders');
        if (!response.ok) throw new Error('Failed to load orders');

        const orders = await response.json();
        updateMonthlyRevenueChart(orders);

    } catch (error) {
        console.error('Error loading monthly revenue data:', error);
    }
}

// Initialize monthly chart
function initializeMonthlyChart() {
    const ctx = document.getElementById('monthlyRevenueChart')?.getContext('2d');
    if (!ctx) {
        console.log('Monthly revenue chart canvas not found');
        return;
    }

    // Destroy existing chart
    if (monthlyRevenueChart) {
        monthlyRevenueChart.destroy();
    }

    monthlyRevenueChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Monthly Revenue ($)',
                data: [],
                backgroundColor: 'rgba(76, 175, 80, 0.8)',
                borderColor: 'rgba(76, 175, 80, 1)',
                borderWidth: 2,
                borderRadius: 4,
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `Revenue: $${context.parsed.y.toLocaleString()}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toLocaleString();
                        }
                    },
                    title: {
                        display: true,
                        text: 'Revenue ($)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Month'
                    }
                }
            }
        }
    });
}

// Update monthly revenue chart with order data
function updateMonthlyRevenueChart(orders) {
    if (!monthlyRevenueChart) {
        console.log('Monthly revenue chart not initialized');
        return;
    }

    // Group orders by month and year
    const monthlyData = {};

    orders.forEach(order => {
        const orderDate = new Date(order.placeDate);
        const monthYear = orderDate.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short'
        });

        if (!monthlyData[monthYear]) {
            monthlyData[monthYear] = 0;
        }
        monthlyData[monthYear] += order.totalAmount;
    });

    // Sort months chronologically
    const sortedMonths = Object.keys(monthlyData).sort((a, b) => {
        return new Date(a) - new Date(b);
    });

    const labels = sortedMonths;
    const data = sortedMonths.map(month => monthlyData[month]);

    monthlyRevenueChart.data.labels = labels;
    monthlyRevenueChart.data.datasets[0].data = data;
    monthlyRevenueChart.update('active');
}

// Update report filters based on report type
function updateReportFilters() {
    const reportType = document.getElementById('reportType').value;
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    const endDate = new Date();
    let startDate = new Date();

    switch (reportType) {
        case 'DAILY':
            startDate.setDate(endDate.getDate() - 1);
            break;
        case 'WEEKLY':
            startDate.setDate(endDate.getDate() - 7);
            break;
        case 'MONTHLY':
            startDate.setMonth(endDate.getMonth() - 1);
            break;
        case 'YEARLY':
            startDate.setFullYear(endDate.getFullYear() - 1);
            break;
    }

    if (startDateInput && endDateInput) {
        startDateInput.value = startDate.toISOString().split('T')[0];
        endDateInput.value = endDate.toISOString().split('T')[0];
    }

    // Reload data with new filters
    loadSummaryData();
}

// Load generated reports
async function loadGeneratedReports() {
    try {
        const response = await fetch('/api/summary-reports');
        if (!response.ok) throw new Error('Failed to load reports');

        const reports = await response.json();
        refreshReportsTable(reports);

    } catch (error) {
        console.error('Error loading reports:', error);
        // If the API endpoint doesn't exist, show empty table
        refreshReportsTable([]);
    }
}

// Refresh reports table
function refreshReportsTable(reports) {
    const tbody = document.querySelector('#reportsTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (reports.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No reports generated yet</td></tr>';
        return;
    }

    reports.forEach(report => {
        const row = document.createElement('tr');
        const periodStart = new Date(report.periodStart).toLocaleDateString();
        const periodEnd = new Date(report.periodEnd).toLocaleDateString();
        const generatedAt = new Date(report.generatedAt).toLocaleString();

        row.innerHTML = `
            <td>${report.reportName || 'Unnamed Report'}</td>
            <td>${report.reportType || 'N/A'}</td>
            <td>${periodStart} to ${periodEnd}</td>
            <td>${report.totalOrders || 0}</td>
            <td>$${(report.totalRevenue || 0).toLocaleString()}</td>
            <td>${generatedAt}</td>
            <td>
                <button class="btn btn-primary btn-sm" onclick="downloadReport(${report.id})">
                    <i class="fas fa-download"></i> CSV
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteReport(${report.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Generate report function
async function generateReport() {
    try {
        const reportType = document.getElementById('reportType').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (!startDate || !endDate) {
            showAlert('Please select start and end dates', 'error');
            return;
        }

        const startDateTime = new Date(startDate + 'T00:00:00');
        const endDateTime = new Date(endDate + 'T23:59:59');

        // Try different API endpoints or methods
        let response;
        try {
            // Try with form data first
            const formData = new URLSearchParams();
            formData.append('reportType', reportType);
            formData.append('startDate', startDateTime.toISOString());
            formData.append('endDate', endDateTime.toISOString());
            formData.append('generatedBy', 'SalesManager');

            response = await fetch('/api/summary-reports/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData
            });
        } catch (apiError) {
            console.log('First API method failed, trying alternative...');
            // Alternative: try with JSON
            response = await fetch('/api/summary-reports/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    reportType: reportType,
                    startDate: startDateTime.toISOString(),
                    endDate: endDateTime.toISOString(),
                    generatedBy: 'SalesManager'
                })
            });
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to generate report');
        }

        const report = await response.json();
        showAlert('Report generated successfully!', 'success');

        // Refresh the reports list
        loadGeneratedReports();

        // Refresh the summary data
        loadSummaryData();

    } catch (error) {
        console.error('Error generating report:', error);

        // If API is not available, simulate success for demo purposes
        if (error.message.includes('Failed to fetch') || error.message.includes('404')) {
            showAlert('Report generation simulated (API endpoint not available)', 'success');
            // Refresh UI anyway
            loadGeneratedReports();
            loadSummaryData();
        } else {
            showAlert('Error generating report: ' + error.message, 'error');
        }
    }
}

// Download report function
async function downloadReport(reportId) {
    try {
        const response = await fetch(`/api/summary-reports/${reportId}/download`);
        if (!response.ok) throw new Error('Failed to download report');

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `order_report_${reportId}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        showAlert('Report downloaded successfully!', 'success');

    } catch (error) {
        console.error('Error downloading report:', error);
        showAlert('Error downloading report: ' + error.message, 'error');
    }
}

// Delete report function
async function deleteReport(reportId) {
    if (!confirm('Are you sure you want to delete this report?')) {
        return;
    }

    try {
        const response = await fetch(`/api/summary-reports/${reportId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete report');

        showAlert('Report deleted successfully!', 'success');
        loadGeneratedReports();

    } catch (error) {
        console.error('Error deleting report:', error);
        showAlert('Error deleting report: ' + error.message, 'error');
    }
}

// Start auto-refresh for summary reports
function startSummaryAutoRefresh() {
    setInterval(() => {
        if (document.getElementById('summaryReport').classList.contains('active')) {
            console.log('Auto-refreshing summary data...');
            loadSummaryData();
        }
    }, 30000); // Refresh every 30 seconds
}


// Update quick stats after order changes to refresh summary report
function updateQuickStatsAfterOrderChange() {
    loadQuickStats();

    // Also update summary report if it's currently active
    if (document.getElementById('summaryReport').classList.contains('active')) {
        loadSummaryData();
    }
}

// Keep your existing generateReport, downloadReport, deleteReport functions
// but ensure they call loadSummaryData() after operations

async function generateReport() {
    try {
        const reportType = document.getElementById('reportType').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (!startDate || !endDate) {
            showAlert('Please select start and end dates', 'error');
            return;
        }

        const startDateTime = new Date(startDate + 'T00:00:00');
        const endDateTime = new Date(endDate + 'T23:59:59');

        const response = await fetch('/api/summary-reports/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `reportType=${reportType}&startDate=${startDateTime.toISOString()}&endDate=${endDateTime.toISOString()}&generatedBy=SalesManager`
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to generate report');
        }

        const report = await response.json();
        showAlert('Report generated successfully!', 'success');
        loadGeneratedReports();

        // Refresh the summary data after generating report
        loadSummaryData();

    } catch (error) {
        console.error('Error generating report:', error);
        showAlert('Error generating report: ' + error.message, 'error');
    }
}

// Export functions
window.loadSummaryReport = loadSummaryReport;
window.generateReport = generateReport;
window.downloadReport = downloadReport;
window.deleteReport = deleteReport;

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