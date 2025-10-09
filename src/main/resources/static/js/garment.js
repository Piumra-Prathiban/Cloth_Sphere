// ============================================
// GARMENT MANAGEMENT - COMPLETE JAVASCRIPT
// ============================================

let garmentsList = [];
let movementsList = [];
let garmentChart, garmentTypeChart;
let existingGarmentIds = [];
let existingTypes = [];
let existingSizes = [];
let selectedGarmentStock = 0;
let validationTimeout;

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    initializeGarmentManagement();
});

function initializeGarmentManagement() {
    setupEventListeners();
    initializeCharts();
    loadFabricsForDropdown();
    loadGarments();
    loadMovements();
    loadStats();
    setTodayDate();
}

// ============================================
// EVENT LISTENERS
// ============================================

function setupEventListeners() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');

    menuToggle?.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('show');
    });

    overlay?.addEventListener('click', () => {
        sidebar.classList.remove('open');
        overlay.classList.remove('show');
    });

    // Garment Modal
    document.getElementById('addNewGarmentBtn')?.addEventListener('click', () => {
        document.getElementById('addGarmentModal').style.display = 'block';
        document.getElementById('submitGarmentBtn').disabled = false;
    });
    document.getElementById('closeGarmentModal')?.addEventListener('click', closeGarmentModalFn);
    document.getElementById('cancelGarmentBtn')?.addEventListener('click', closeGarmentModalFn);
    document.getElementById('addGarmentForm')?.addEventListener('submit', handleGarmentSubmit);

    // Edit Garment Modal
    document.getElementById('closeEditGarmentModal')?.addEventListener('click', closeEditGarmentModalFn);
    document.getElementById('cancelEditGarmentBtn')?.addEventListener('click', closeEditGarmentModalFn);
    document.getElementById('editGarmentForm')?.addEventListener('submit', handleEditGarmentSubmit);

    // Movement Modal
    document.getElementById('addMovementBtn')?.addEventListener('click', () => {
        document.getElementById('addMovementModal').style.display = 'block';
        setTodayDate();
    });
    document.getElementById('closeMovementModal')?.addEventListener('click', closeMovementModalFn);
    document.getElementById('cancelMovementBtn')?.addEventListener('click', closeMovementModalFn);
    document.getElementById('addMovementForm')?.addEventListener('submit', handleMovementSubmit);

    // Dynamic Updates
    document.getElementById('garmentSelect')?.addEventListener('change', updateGarmentDetails);
    document.getElementById('movementQuantity')?.addEventListener('input', validateQuantity);

    // Validation
    document.getElementById('newGarmentId')?.addEventListener('input', validateGarmentId);
    document.getElementById('newGarmentId')?.addEventListener('blur', validateGarmentId);
    document.getElementById('initialStock')?.addEventListener('input', validateInitialStock);
    document.getElementById('initialStock')?.addEventListener('blur', validateInitialStock);

    // Filters and Sorting
    document.getElementById('sortGarmentSelect')?.addEventListener('change', applySortAndFilter);
    document.getElementById('filterGarmentType')?.addEventListener('change', applySortAndFilter);
    document.getElementById('filterGarmentSize')?.addEventListener('change', applySortAndFilter);
    document.getElementById('sortSelect')?.addEventListener('change', applyMovementSortAndFilter);
    document.getElementById('filterType')?.addEventListener('change', applyMovementSortAndFilter);
    document.getElementById('filterStatus')?.addEventListener('change', applyMovementSortAndFilter);

    // Click Outside Modals
    window.addEventListener('click', (e) => {
        if (e.target === document.getElementById('addGarmentModal')) closeGarmentModalFn();
        if (e.target === document.getElementById('editGarmentModal')) closeEditGarmentModalFn();
        if (e.target === document.getElementById('addMovementModal')) closeMovementModalFn();
    });

    // Action Buttons
    document.addEventListener('click', handleActions);
}

// ============================================
// CHARTS INITIALIZATION
// ============================================

function initializeCharts() {
    const garmentCtx = document.getElementById('garmentChart')?.getContext('2d');
    if (garmentCtx) {
        garmentChart = new Chart(garmentCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Stock Level (pieces)',
                    data: [],
                    backgroundColor: 'rgba(52, 152, 219, 0.8)',
                    borderColor: 'rgba(52, 152, 219, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.1)' } },
                    x: { grid: { color: 'rgba(0,0,0,0.1)' } }
                }
            }
        });
    }

    const typeCtx = document.getElementById('garmentTypeChart')?.getContext('2d');
    if (typeCtx) {
        garmentTypeChart = new Chart(typeCtx, {
            type: 'doughnut',
            data: {
                labels: [],
                datasets: [{
                    data: [],
                    backgroundColor: [
                        'rgba(52, 152, 219, 0.8)', 'rgba(39, 174, 96, 0.8)',
                        'rgba(243, 156, 18, 0.8)', 'rgba(231, 76, 60, 0.8)',
                        'rgba(155, 89, 182, 0.8)', 'rgba(52, 73, 94, 0.8)',
                        'rgba(241, 196, 15, 0.8)'
                    ],
                    borderColor: '#fff',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { padding: 15, usePointStyle: true }
                    }
                }
            }
        });
    }
}

// ============================================
// DATA LOADING
// ============================================

function loadStats() {
    fetch('/api/garments/dashboard/stats')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            document.getElementById('totalGarmentTypes').textContent = data.totalGarmentTypes || 0;
            document.getElementById('totalPieces').textContent = data.totalPieces || 0;
            document.getElementById('producedToday').textContent = data.stockInToday || 0;
            document.getElementById('shippedToday').textContent = data.shippedToday || 0;
        })
        .catch(err => {
            console.error('Error loading stats:', err);
            showNotification('Failed to load stats: ' + err.message, 'error');
        });
}

function loadGarments() {
    showLoadingState('garmentTableBody', 'Loading garments...');

    fetch('/api/garments/all')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            garmentsList = Array.isArray(data) ? data : [];
            existingGarmentIds = garmentsList.map(g => g.garmentId);
            existingTypes = [...new Set(garmentsList.map(g => g.garmentType))].sort();

            displayGarments();
            updateGarmentDropdown();
            updateCharts();
            populateDynamicDropdowns();
            populateGarmentFilters();
        })
        .catch(err => {
            console.error('Error loading garments:', err);
            document.getElementById('garmentTableBody').innerHTML =
                '<tr><td colspan="6" style="text-align:center;color:#e74c3c;padding:30px;"><i class="fas fa-exclamation-triangle"></i><br>Failed to load garments: ' + err.message + '</td></tr>';
            garmentsList = [];
            existingGarmentIds = [];
            existingTypes = [];
        });
}

function loadMovements() {
    showLoadingState('movementTableBody', 'Loading movements...');

    fetch('/api/garment-movements')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            movementsList = Array.isArray(data) ? data : [];
            displayMovements();
            populateMovementFilters();
        })
        .catch(err => {
            console.error('Error loading movements:', err);
            document.getElementById('movementTableBody').innerHTML =
                '<tr><td colspan="8" style="text-align:center;color:#e74c3c;padding:30px;"><i class="fas fa-exclamation-triangle"></i><br>Failed to load movements: ' + err.message + '</td></tr>';
            movementsList = [];
        });
}

function showLoadingState(elementId, message = 'Loading...') {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `
            <tr>
                <td colspan="100%" style="text-align:center;padding:30px;color:#666;">
                    <i class="fas fa-spinner fa-spin" style="font-size:24px;margin-bottom:10px;"></i>
                    <br>${message}
                </td>
            </tr>
        `;
    }
}

function loadFabricsForDropdown() {
    fetch('/api/fabrics')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(fabrics => {
            const fabricSelect = document.getElementById('newGarmentFabricId');
            const editFabricSelect = document.getElementById('editGarmentFabricId');

            if (fabricSelect) {
                fabricSelect.innerHTML = '<option value="">Select Fabric ID</option>';
                fabrics.forEach(f => {
                    const option = document.createElement('option');
                    option.value = f.fabricId;
                    option.textContent = `${f.fabricId} - ${f.fabricType} (${f.color}) - Stock: ${f.currentStock || 0}m`;
                    fabricSelect.appendChild(option);
                });
            }

            if (editFabricSelect) {
                editFabricSelect.innerHTML = '<option value="">Select Fabric ID</option>';
                fabrics.forEach(f => {
                    const option = document.createElement('option');
                    option.value = f.fabricId;
                    option.textContent = `${f.fabricId} - ${f.fabricType} (${f.color}) - Stock: ${f.currentStock || 0}m`;
                    editFabricSelect.appendChild(option);
                });
            }
        })
        .catch(err => {
            console.error('Error loading fabrics:', err);
            showNotification('Failed to load fabrics: ' + err.message, 'error');
        });
}

// ============================================
// MODAL FUNCTIONS
// ============================================

function closeGarmentModalFn() {
    document.getElementById('addGarmentModal').style.display = 'none';
    document.getElementById('addGarmentForm').reset();
    document.getElementById('garmentIdHelp').style.display = 'block';
    document.getElementById('garmentIdError').style.display = 'none';
    document.getElementById('stockHelp').style.display = 'block';
    document.getElementById('stockError').style.display = 'none';
    document.getElementById('duplicateWarning').style.display = 'none';
    document.getElementById('newGarmentId').classList.remove('input-error');
    document.getElementById('initialStock').classList.remove('input-error');
    document.getElementById('submitGarmentBtn').disabled = false;
}

function closeEditGarmentModalFn() {
    document.getElementById('editGarmentModal').style.display = 'none';
    document.getElementById('editGarmentForm').reset();
}

function closeMovementModalFn() {
    document.getElementById('addMovementModal').style.display = 'none';
    document.getElementById('addMovementForm').reset();
    document.getElementById('garmentDetails').innerHTML = 'Select a garment to see details';
    document.getElementById('quantityHelp').style.display = 'block';
    document.getElementById('quantityError').style.display = 'none';
    document.getElementById('movementQuantity').classList.remove('input-error');
    document.getElementById('submitMovementBtn').disabled = false;
    selectedGarmentStock = 0;
    setTodayDate();
}

function setTodayDate() {
    const today = new Date().toISOString().split('T')[0];
    const movementDate = document.getElementById('movementDate');

    if (movementDate) {
        movementDate.value = today;
        movementDate.min = today;
        movementDate.max = today;
    }
}

// ============================================
// DISPLAY FUNCTIONS
// ============================================

function displayGarments() {
    applySortAndFilter();
}

function applySortAndFilter() {
    const sortValue = document.getElementById('sortGarmentSelect')?.value || '';
    const typeFilter = document.getElementById('filterGarmentType')?.value || '';
    const sizeFilter = document.getElementById('filterGarmentSize')?.value || '';

    let filteredGarments = [...garmentsList];

    if (typeFilter) {
        filteredGarments = filteredGarments.filter(g => g.garmentType === typeFilter);
    }
    if (sizeFilter) {
        filteredGarments = filteredGarments.filter(g => g.size === sizeFilter);
    }

    if (sortValue === 'stock-desc') {
        filteredGarments.sort((a, b) => (b.currentStock || 0) - (a.currentStock || 0));
    } else if (sortValue === 'stock-asc') {
        filteredGarments.sort((a, b) => (a.currentStock || 0) - (b.currentStock || 0));
    } else if (sortValue === 'id-asc') {
        filteredGarments.sort((a, b) => a.garmentId.localeCompare(b.garmentId));
    } else if (sortValue === 'id-desc') {
        filteredGarments.sort((a, b) => b.garmentId.localeCompare(a.garmentId));
    }

    displayGarmentsFiltered(filteredGarments);
}

function displayGarmentsFiltered(garments) {
    const tbody = document.getElementById('garmentTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (garments.length > 0) {
        garments.forEach(g => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${g.garmentId}</td>
                <td>${g.garmentType}</td>
                <td>${g.size}</td>
                <td>${g.fabricId}</td>
                <td>${g.currentStock || 0}</td>
                <td class="action-buttons-table">
                    <button class="btn edit btn-edit-garment" data-id="${g.garmentId}" data-type="${g.garmentType}" data-size="${g.size}" data-fabric="${g.fabricId}">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn delete btn-delete-garment" data-id="${g.garmentId}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#666;padding:30px;">No garments match the selected filters</td></tr>';
    }
}

function displayMovements() {
    applyMovementSortAndFilter();
}

function applyMovementSortAndFilter() {
    const sortValue = document.getElementById('sortSelect')?.value || '';
    const typeFilter = document.getElementById('filterType')?.value || '';
    const statusFilter = document.getElementById('filterStatus')?.value || '';

    let filteredMovements = [...movementsList];

    if (typeFilter) {
        filteredMovements = filteredMovements.filter(m => {
            const garment = garmentsList.find(g => g.garmentId === m.garmentId);
            return garment && garment.garmentType === typeFilter;
        });
    }
    if (statusFilter) {
        filteredMovements = filteredMovements.filter(m => m.status === statusFilter);
    }

    if (sortValue === 'date-desc') {
        filteredMovements.sort((a, b) => new Date(b.movementDate) - new Date(a.movementDate));
    } else if (sortValue === 'date-asc') {
        filteredMovements.sort((a, b) => new Date(a.movementDate) - new Date(b.movementDate));
    } else if (sortValue === 'quantity-desc') {
        filteredMovements.sort((a, b) => b.quantity - a.quantity);
    } else if (sortValue === 'quantity-asc') {
        filteredMovements.sort((a, b) => a.quantity - b.quantity);
    }

    displayMovementsFiltered(filteredMovements);
}

function displayMovementsFiltered(movements) {
    const tbody = document.getElementById('movementTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (movements.length > 0) {
        movements.forEach(m => {
            const garment = garmentsList.find(g => g.garmentId === m.garmentId);
            const statusClass = m.status === 'In' ? 'status-in' :
                m.status === 'Shipped' ? 'status-shipped' : 'status-out';
            const statusText = m.status === 'In' ? 'Stock In' :
                m.status === 'Shipped' ? 'Shipped' : 'Stock Out';

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${m.movementId}</td>
                <td>${m.garmentId}</td>
                <td>${garment ? garment.fabricId : 'N/A'}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${m.movementDate}</td>
                <td>${m.quantity}</td>
                <td>${m.totalQuantity || 'N/A'}</td>
                <td class="action-buttons-table">
                    <button class="btn delete btn-delete-movement" data-id="${m.movementId}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#666;padding:30px;">No movements match the selected filters</td></tr>';
    }
}

// ============================================
// CHARTS UPDATE
// ============================================

function updateCharts() {
    if (!garmentChart || !garmentTypeChart) return;

    if (garmentsList.length === 0) {
        garmentChart.data.labels = ['No Data'];
        garmentChart.data.datasets[0].data = [0];
        garmentChart.update();

        garmentTypeChart.data.labels = ['No Data'];
        garmentTypeChart.data.datasets[0].data = [0];
        garmentTypeChart.update();
        return;
    }

    const labels = garmentsList.map(g => `${g.garmentType} - ${g.size}`);
    const stocks = garmentsList.map(g => g.currentStock || 0);

    garmentChart.data.labels = labels;
    garmentChart.data.datasets[0].data = stocks;
    garmentChart.update();

    const typeData = calculateTypeDistribution(labels, stocks);
    garmentTypeChart.data.labels = typeData.labels;
    garmentTypeChart.data.datasets[0].data = typeData.values;
    garmentTypeChart.update();
}

function calculateTypeDistribution(labels, quantities) {
    const typeMap = {};
    labels.forEach((label, index) => {
        const type = label.split(' - ')[0];
        if (!typeMap[type]) typeMap[type] = 0;
        typeMap[type] += quantities[index];
    });
    return {
        labels: Object.keys(typeMap),
        values: Object.values(typeMap)
    };
}

// ============================================
// FILTERS & DROPDOWNS
// ============================================

function populateGarmentFilters() {
    const typeFilter = document.getElementById('filterGarmentType');
    const sizeFilter = document.getElementById('filterGarmentSize');

    if (typeFilter) {
        typeFilter.innerHTML = '<option value="">All Types</option>';
        existingTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = type;
            typeFilter.appendChild(option);
        });
    }

    if (sizeFilter) {
        sizeFilter.innerHTML = '<option value="">All Sizes</option>';
        existingSizes.forEach(size => {
            const option = document.createElement('option');
            option.value = size;
            option.textContent = size;
            sizeFilter.appendChild(option);
        });
    }
}

function populateMovementFilters() {
    const typeFilter = document.getElementById('filterType');
    if (!typeFilter) return;

    const types = [...new Set(movementsList.map(m => {
        const garment = garmentsList.find(g => g.garmentId === m.garmentId);
        return garment ? garment.garmentType : null;
    }).filter(t => t))].sort();

    typeFilter.innerHTML = '<option value="">All Garment Types</option>';
    types.forEach(type => {
        const option = document.createElement('option');
        option.value = type;
        option.textContent = type;
        typeFilter.appendChild(option);
    });
}

function updateGarmentDropdown() {
    const select = document.getElementById('garmentSelect');
    if (!select) return;

    select.innerHTML = '<option value="">Choose Garment</option>';

    garmentsList.forEach(g => {
        const option = document.createElement('option');
        option.value = g.garmentId;
        option.textContent = `${g.garmentId} - ${g.garmentType} (${g.size}) - Stock: ${g.currentStock || 0} pcs`;
        option.dataset.type = g.garmentType;
        option.dataset.size = g.size;
        option.dataset.fabric = g.fabricId;
        option.dataset.stock = g.currentStock || 0;
        select.appendChild(option);
    });
}

function populateDynamicDropdowns() {
    const typeSelect = document.getElementById('newGarmentType');

    if (typeSelect) {
        // Remove existing event listeners by replacing the element
        const newTypeSelect = typeSelect.cloneNode(false);
        typeSelect.parentNode.replaceChild(newTypeSelect, typeSelect);

        newTypeSelect.innerHTML = '<option value="">Select Garment Type</option>';
        existingTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = type;
            newTypeSelect.appendChild(option);
        });

        const addTypeOption = document.createElement('option');
        addTypeOption.value = '_new_';
        addTypeOption.textContent = '➕ Add New Type...';
        addTypeOption.style.fontWeight = 'bold';
        addTypeOption.style.color = '#4B0082';
        newTypeSelect.appendChild(addTypeOption);

        // Add event listeners
        newTypeSelect.addEventListener('change', function() {
            if (this.value === '_new_') {
                const newType = prompt('Enter new garment type:');
                if (newType && newType.trim()) {
                    const trimmedType = newType.trim();
                    if (!existingTypes.includes(trimmedType)) {
                        existingTypes.push(trimmedType);
                        existingTypes.sort();
                        populateDynamicDropdowns();
                        document.getElementById('newGarmentType').value = trimmedType;
                    } else {
                        this.value = trimmedType;
                    }
                } else {
                    this.value = '';
                }
            }
            checkDuplicateTypeSizeFabric();
        });

        // Re-attach validation listeners
        newTypeSelect.addEventListener('change', checkDuplicateTypeSizeFabric);
        document.getElementById('newGarmentSize')?.addEventListener('change', checkDuplicateTypeSizeFabric);
        document.getElementById('newGarmentFabricId')?.addEventListener('change', checkDuplicateTypeSizeFabric);
    }
}

// ============================================
// VALIDATION FUNCTIONS
// ============================================

function validateGarmentId() {
    const input = document.getElementById('newGarmentId');
    const helpText = document.getElementById('garmentIdHelp');
    const errorText = document.getElementById('garmentIdError');

    if (!input) return false;

    const garmentId = input.value.trim().toUpperCase();
    input.value = garmentId;

    helpText.style.display = 'block';
    errorText.style.display = 'none';
    input.classList.remove('input-error');

    if (garmentId === '') {
        return false;
    }

    const formatRegex = /^GAR\d{3}$/;
    if (!formatRegex.test(garmentId)) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Invalid format! Use GAR followed by 3 digits (e.g., GAR001)';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    if (existingGarmentIds.includes(garmentId)) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> This Garment ID already exists!';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    input.style.borderColor = '#27ae60';
    return true;
}

function validateInitialStock() {
    const input = document.getElementById('initialStock');
    const helpText = document.getElementById('stockHelp');
    const errorText = document.getElementById('stockError');

    if (!input) return false;

    const value = parseInt(input.value);

    helpText.style.display = 'block';
    errorText.style.display = 'none';
    input.classList.remove('input-error');

    if (isNaN(value) || input.value === '') {
        return false;
    }

    if (value < 10) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Initial stock must be at least 10 pieces!';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    input.style.borderColor = '#27ae60';
    return true;
}

function checkDuplicateTypeSizeFabric() {
    const typeSelect = document.getElementById('newGarmentType');
    const sizeSelect = document.getElementById('newGarmentSize');
    const fabricSelect = document.getElementById('newGarmentFabricId');
    const warningDiv = document.getElementById('duplicateWarning');

    if (!typeSelect || !sizeSelect || !fabricSelect || !warningDiv) return false;

    if (!typeSelect.value || !sizeSelect.value || !fabricSelect.value || typeSelect.value === '_new_') {
        warningDiv.style.display = 'none';
        return false;
    }

    const isDuplicate = garmentsList.some(g =>
        g.garmentType === typeSelect.value &&
        g.size === sizeSelect.value &&
        g.fabricId === fabricSelect.value
    );

    if (isDuplicate) {
        warningDiv.style.display = 'block';
        warningDiv.innerHTML = '<i class="fas fa-exclamation-triangle"></i> This type+size+fabric combination already exists! Each garment must have a unique combination.';
        return true;
    } else {
        warningDiv.style.display = 'none';
        return false;
    }
}

function updateGarmentDetails() {
    const select = document.getElementById('garmentSelect');
    const selectedOption = select?.options[select.selectedIndex];
    const detailsDiv = document.getElementById('garmentDetails');

    if (!select || !detailsDiv) return;

    if (select.value && selectedOption) {
        const stock = parseInt(selectedOption.dataset.stock) || 0;
        selectedGarmentStock = stock;

        detailsDiv.innerHTML = `
            <strong>Garment ID:</strong> ${select.value}<br>
            <strong>Type:</strong> ${selectedOption.dataset.type}<br>
            <strong>Size:</strong> ${selectedOption.dataset.size}<br>
            <strong>Fabric ID:</strong> ${selectedOption.dataset.fabric}<br>
            <strong>Current Stock:</strong> ${stock} pieces
        `;
    } else {
        detailsDiv.innerHTML = 'Select a garment to see details';
        selectedGarmentStock = 0;
    }
}

function validateQuantity() {
    const input = document.getElementById('movementQuantity');
    const statusSelect = document.getElementById('movementStatus');
    const helpText = document.getElementById('quantityHelp');
    const errorText = document.getElementById('quantityError');

    if (!input || !statusSelect || !helpText || !errorText) return true;

    if (!statusSelect.value) return true;

    const quantity = parseInt(input.value);

    helpText.style.display = 'block';
    errorText.style.display = 'none';
    input.classList.remove('input-error');

    if (isNaN(quantity) || input.value === '') {
        return true;
    }

    if (quantity <= 0) {
        helpText.style.display = 'none';
        errorText.innerHTML = '<i class="fas fa-exclamation-circle"></i> Quantity must be greater than 0';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    if (statusSelect.value === 'Shipped' && quantity > selectedGarmentStock) {
        helpText.style.display = 'none';
        errorText.innerHTML = `<i class="fas fa-exclamation-circle"></i> Cannot ship ${quantity} pieces. Only ${selectedGarmentStock} pieces available in stock.`;
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    input.style.borderColor = '#27ae60';
    return true;
}

function validateMovementBeforeSubmit() {
    const garmentSelect = document.getElementById('garmentSelect');
    const statusSelect = document.getElementById('movementStatus');
    const quantityInput = document.getElementById('movementQuantity');

    if (!garmentSelect?.value) {
        showNotification('Please select a garment', 'error');
        garmentSelect?.focus();
        return false;
    }

    if (!statusSelect?.value) {
        showNotification('Please select movement type', 'error');
        statusSelect?.focus();
        return false;
    }

    const quantity = parseInt(quantityInput?.value);
    if (isNaN(quantity) || quantity <= 0) {
        showNotification('Please enter a valid quantity', 'error');
        quantityInput?.focus();
        return false;
    }

    if (statusSelect.value === 'Shipped' && quantity > selectedGarmentStock) {
        showNotification(`Quantity (${quantity} pcs) exceeds available stock (${selectedGarmentStock} pcs)!`, 'error');
        quantityInput?.focus();
        return false;
    }

    return true;
}

// ============================================
// FORM SUBMISSION HANDLERS
// ============================================

function handleGarmentSubmit(e) {
    e.preventDefault();

    // Force validation before checking
    const garmentIdValid = validateGarmentId();
    const stockValid = validateInitialStock();
    const isDuplicate = checkDuplicateTypeSizeFabric();

    if (!garmentIdValid || !stockValid) {
        showNotification('Please fix all errors before submitting', 'error');
        return;
    }

    if (isDuplicate) {
        showNotification('Cannot add garment: This type+size+fabric combination already exists!', 'error');
        return;
    }

    const form = e.target;
    const garmentId = form.garmentId.value.trim().toUpperCase();
    const fabricId = form.fabricId.value;
    const garmentType = form.garmentType.value.trim();
    const size = form.size.value;
    const initialStock = parseInt(form.initialStock.value);

    if (!garmentId || !fabricId || !garmentType || !size || isNaN(initialStock)) {
        showNotification('Please fill in all required fields', 'error');
        return;
    }

    const data = {
        garmentId: garmentId,
        fabricId: fabricId,
        garmentType: garmentType,
        size: size,
        initialStock: initialStock
    };

    const submitBtn = document.getElementById('submitGarmentBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';

    fetch('/api/garments/new', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp.success) {
                showNotification(`Garment ${garmentId} added with initial stock of ${initialStock} pieces!`, 'success');
                closeGarmentModalFn();
                loadGarments();
                loadMovements();
                loadStats();
            } else {
                showNotification(resp.message || 'Error adding garment', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-plus-circle"></i> Add Garment';
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-plus-circle"></i> Add Garment';
        });
}

function handleEditGarmentSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const garmentId = form.garmentId.value;

    const data = {
        fabricId: form.fabricId.value,
        garmentType: form.garmentType.value.trim(),
        size: form.size.value
    };

    const submitBtn = e.submitter || form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';
    }

    fetch(`/api/garments/${garmentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp.success) {
                showNotification('Garment updated successfully!', 'success');
                closeEditGarmentModalFn();
                loadGarments();
            } else {
                showNotification(resp.message || 'Error updating garment', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-edit"></i> Update Garment';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-edit"></i> Update Garment';
            }
        });
}

function generateMovementId() {
    if (movementsList.length === 0) return 'GMI001';
    const lastId = movementsList.map(m => m.movementId).sort().pop();
    const num = parseInt(lastId.substring(3)) + 1;
    return 'GMI' + String(num).padStart(3, '0');
}

function handleMovementSubmit(e) {
    e.preventDefault();

    if (!validateMovementBeforeSubmit()) {
        return;
    }

    const form = e.target;
    const garmentId = form.garmentId.value;
    const status = form.status.value;
    const quantity = parseInt(form.quantity.value);

    const movementId = generateMovementId();

    const data = {
        movementId: movementId,
        garmentId: garmentId,
        status: status,
        movementDate: form.movementDate.value,
        quantity: quantity
    };

    const submitBtn = document.getElementById('submitMovementBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Recording...';
    }

    fetch('/api/garment-movements', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp.success) {
                showNotification(`Movement ${movementId} recorded successfully!`, 'success');
                closeMovementModalFn();
                loadMovements();
                loadGarments();
                loadStats();
            } else {
                showNotification(resp.message || 'Error recording movement', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-exchange-alt"></i> Record Movement';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-exchange-alt"></i> Record Movement';
            }
        });
}

// ============================================
// ACTION HANDLERS
// ============================================

function handleActions(e) {
    const editGarmentBtn = e.target.closest('.btn-edit-garment');
    const deleteGarmentBtn = e.target.closest('.btn-delete-garment');
    const deleteMovementBtn = e.target.closest('.btn-delete-movement');

    if (editGarmentBtn) {
        const id = editGarmentBtn.dataset.id;
        const type = editGarmentBtn.dataset.type;
        const size = editGarmentBtn.dataset.size;
        const fabric = editGarmentBtn.dataset.fabric;

        document.getElementById('editGarmentId').value = id;
        document.getElementById('editGarmentType').value = type;
        document.getElementById('editGarmentSize').value = size;
        document.getElementById('editGarmentFabricId').value = fabric;
        document.getElementById('editGarmentModal').style.display = 'block';
    }

    if (deleteGarmentBtn) {
        const id = deleteGarmentBtn.dataset.id;
        showConfirmDialog(
            `Are you sure you want to delete garment <strong>${id}</strong>?<br><br>This will also delete all associated movements and cannot be undone.`,
            () => {
                deleteGarmentBtn.disabled = true;
                deleteGarmentBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

                fetch(`/api/garments/${id}`, { method: 'DELETE' })
                    .then(res => {
                        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                        return res.json();
                    })
                    .then(resp => {
                        if (resp.success) {
                            showNotification('Garment deleted successfully', 'success');
                            loadGarments();
                            loadMovements();
                            loadStats();
                        } else {
                            showNotification(resp.message || 'Error deleting garment', 'error');
                            deleteGarmentBtn.disabled = false;
                            deleteGarmentBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        showNotification('Error deleting garment: ' + err.message, 'error');
                        deleteGarmentBtn.disabled = false;
                        deleteGarmentBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                    });
            }
        );
    }

    if (deleteMovementBtn) {
        const id = deleteMovementBtn.dataset.id;
        showConfirmDialog(
            `Are you sure you want to delete movement <strong>${id}</strong>?<br><br>This will affect stock calculations and cannot be undone.`,
            () => {
                deleteMovementBtn.disabled = true;
                deleteMovementBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

                fetch(`/api/garment-movements/${id}`, { method: 'DELETE' })
                    .then(res => {
                        if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                        return res.json();
                    })
                    .then(resp => {
                        if (resp.success) {
                            showNotification('Movement deleted successfully', 'success');
                            loadMovements();
                            loadGarments();
                            loadStats();
                        } else {
                            showNotification(resp.message || 'Error deleting movement', 'error');
                            deleteMovementBtn.disabled = false;
                            deleteMovementBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        showNotification('Error deleting movement: ' + err.message, 'error');
                        deleteMovementBtn.disabled = false;
                        deleteMovementBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                    });
            }
        );
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

function showConfirmDialog(message, onConfirm) {
    const existingDialog = document.getElementById('confirmDialog');
    if (existingDialog) existingDialog.remove();

    const dialog = document.createElement('div');
    dialog.id = 'confirmDialog';
    dialog.className = 'modal';
    dialog.style.display = 'block';
    dialog.innerHTML = `
        <div class="modal-content" style="max-width:450px;margin-top:150px;">
            <div class="modal-header">
                <h3 class="modal-title">
                    <i class="fas fa-exclamation-triangle"></i> Confirm Action
                </h3>
            </div>
            <div style="padding:25px;">
                <p style="margin-bottom:20px;color:#555;line-height:1.6;">${message}</p>
                <div class="form-buttons">
                    <button type="button" class="btn btn-cancel" id="confirmCancel">
                        <i class="fas fa-times"></i> Cancel
                    </button>
                    <button type="button" class="btn delete" id="confirmOk">
                        <i class="fas fa-check"></i> Confirm
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(dialog);

    document.getElementById('confirmCancel').onclick = () => dialog.remove();
    document.getElementById('confirmOk').onclick = () => {
        dialog.remove();
        onConfirm();
    };
    dialog.onclick = (e) => {
        if (e.target === dialog) dialog.remove();
    };
}

function showNotification(msg, type = 'info') {
    document.querySelector('.notification')?.remove();

    const iconMap = {
        success: 'check-circle',
        error: 'exclamation-triangle',
        warning: 'exclamation-circle',
        info: 'info-circle'
    };

    const colorMap = {
        success: '#27ae60',
        error: '#e74c3c',
        warning: '#f39c12',
        info: '#3498db'
    };

    const n = document.createElement('div');
    n.className = 'notification';
    n.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        padding: 16px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.15);
        display: flex;
        align-items: center;
        gap: 12px;
        z-index: 10000;
        transform: translateX(400px);
        transition: transform 0.3s ease;
        max-width: 400px;
        border-left: 4px solid ${colorMap[type]};
    `;

    n.innerHTML = `
        <i class="fas fa-${iconMap[type]}" style="color: ${colorMap[type]}; font-size: 20px;"></i>
        <span style="color: #2c3e50; font-size: 14px; font-weight: 500;">${msg}</span>
    `;

    document.body.appendChild(n);

    setTimeout(() => n.style.transform = 'translateX(0)', 100);
    setTimeout(() => {
        n.style.transform = 'translateX(400px)';
        setTimeout(() => n.remove(), 300);
    }, 4000);
}

// ============================================
// KEYBOARD SHORTCUTS
// ============================================

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
        document.getElementById('confirmDialog')?.remove();
    }

    if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
        e.preventDefault();
        document.getElementById('addNewGarmentBtn')?.click();
    }

    if ((e.ctrlKey || e.metaKey) && e.key === 'm') {
        e.preventDefault();
        document.getElementById('addMovementBtn')?.click();
    }
});