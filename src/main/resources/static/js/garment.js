// ============================================
// GARMENT MANAGEMENT - FIXED JAVASCRIPT
// ============================================

let garmentsList = [];
let movementsList = [];
let existingGarmentIds = [];
let existingTypes = [];
let existingSizes = [];
let selectedGarmentStock = 0;

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    initializeGarmentManagement();
});

function initializeGarmentManagement() {
    setupEventListeners();

    // Load initial data and ensure stats are calculated
    Promise.all([
        loadGarments(),
        loadMovements()
    ]).then(() => {
        console.log('Both garments and movements loaded, calculating stats...');
        calculateAndDisplayStats();
    }).catch(err => {
        console.error('Error loading initial data:', err);
        calculateAndDisplayStats();
    });

    setTimeout(() => {
        populateDynamicDropdowns();
    }, 100);
}



function loadMovementsAsync() {
    return new Promise((resolve, reject) => {
        fetch('/api/garments/movements')
            .then(res => {
                if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                return res.json();
            })
            .then(data => {
                movementsList = Array.isArray(data) ? data : [];
                displayMovements();
                populateMovementFilters();
                resolve();
            })
            .catch(err => {
                console.error('Error loading movements:', err);
                movementsList = [];
                reject(err);
            });
    });
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
    const addGarmentBtn = document.getElementById('addNewGarmentBtn');
    if (addGarmentBtn) {
        addGarmentBtn.addEventListener('click', () => {
            document.getElementById('addGarmentModal').style.display = 'block';
            document.getElementById('submitGarmentBtn').disabled = false;
            populateDynamicDropdowns();
            generateGarmentId();
        });
    }

    document.getElementById('closeGarmentModal')?.addEventListener('click', closeGarmentModalFn);
    document.getElementById('cancelGarmentBtn')?.addEventListener('click', closeGarmentModalFn);

    const addGarmentForm = document.getElementById('addGarmentForm');
    if (addGarmentForm) {
        addGarmentForm.addEventListener('submit', handleGarmentSubmit);
    }

    // Movement Modal
    document.getElementById('addMovementBtn')?.addEventListener('click', () => {
        document.getElementById('addMovementModal').style.display = 'block';
        setTodayDate();
        updateGarmentDropdown();
    });
    document.getElementById('closeMovementModal')?.addEventListener('click', closeMovementModalFn);
    document.getElementById('cancelMovementBtn')?.addEventListener('click', closeMovementModalFn);

    const addMovementForm = document.getElementById('addMovementForm');
    if (addMovementForm) {
        addMovementForm.addEventListener('submit', handleMovementSubmit);
    }

    // Approval Modal
    document.getElementById('closeApprovalModal')?.addEventListener('click', closeApprovalModalFn);
    document.getElementById('cancelApprovalBtn')?.addEventListener('click', closeApprovalModalFn);

    const movementApprovalForm = document.getElementById('movementApprovalForm');
    if (movementApprovalForm) {
        movementApprovalForm.addEventListener('submit', handleApprovalSubmit);
    }

    // Dynamic Updates
    document.getElementById('garmentSelect')?.addEventListener('change', updateGarmentDetails);
    document.getElementById('movementQuantity')?.addEventListener('input', validateQuantity);
    document.getElementById('rejectedQuantity')?.addEventListener('input', toggleRejectReason);
    document.getElementById('approvedQuantity')?.addEventListener('input', updateRejectedQuantity);

    // Validation
    document.getElementById('newGarmentType')?.addEventListener('change', checkDuplicateTypeSizeFabric);
    document.getElementById('newGarmentSize')?.addEventListener('change', checkDuplicateTypeSizeFabric);
    document.getElementById('newGarmentFabricId')?.addEventListener('change', checkDuplicateTypeSizeFabric);

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
        if (e.target === document.getElementById('addMovementModal')) closeMovementModalFn();
        if (e.target === document.getElementById('movementApprovalModal')) closeApprovalModalFn();
    });

    // Action Buttons
    document.addEventListener('click', handleActions);
}

// ============================================
// AUTO-GENERATE GARMENT ID
// ============================================

function generateGarmentId() {
    const garmentIdInput = document.getElementById('newGarmentId');
    if (!garmentIdInput) return;

    // Use the new endpoint to get the next garment ID
    fetch('/api/inventory/garments/next-garment-id')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.text();
        })
        .then(nextId => {
            garmentIdInput.value = nextId;
            garmentIdInput.readOnly = true;
        })
        .catch(err => {
            console.error('Error generating garment ID:', err);
            // Fallback to local calculation if API fails
            let maxNum = 0;
            existingGarmentIds.forEach(id => {
                const match = id.match(/GAR(\d{3})/);
                if (match) {
                    const num = parseInt(match[1]);
                    if (num > maxNum) maxNum = num;
                }
            });
            const nextNum = maxNum + 1;
            const nextId = `GAR${String(nextNum).padStart(3, '0')}`;
            garmentIdInput.value = nextId;
            garmentIdInput.readOnly = true;
        });
}

// ============================================
// DATA LOADING
// ============================================

function loadStats() {
    document.getElementById('totalGarmentTypes').textContent = '0';
    document.getElementById('totalPieces').textContent = '0';
    document.getElementById('producedToday').textContent = '0';
    document.getElementById('shippedToday').textContent = '0';

    fetch('/api/garments/stats')
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            document.getElementById('totalGarmentTypes').textContent = data.totalGarmentTypes || 0;
            document.getElementById('totalPieces').textContent = data.totalPieces || 0;
            document.getElementById('producedToday').textContent = data.producedToday || 0;
            document.getElementById('shippedToday').textContent = data.shippedToday || 0;
        })
        .catch(err => {
            console.error('Error loading stats:', err);
            if (garmentsList.length > 0 || movementsList.length > 0) {
                calculateAndDisplayStats();
            }
        });
}

function loadGarments() {
    showLoadingState('garmentTableBody', 'Loading garments...');
    console.log('Loading garments from API...');

    fetch('/api/garments')
        .then(res => {
            console.log('Garments API response status:', res.status);
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            console.log('Loaded garments:', data);
            // FIX: Map backend properties to frontend expected properties
            garmentsList = Array.isArray(data) ? data.map(g => {
                return {
                    ...g,
                    garmentType: g.type, // Map 'type' to 'garmentType' for frontend
                    currentStock: g.currentStock || 0
                };
            }) : [];

            existingGarmentIds = garmentsList.map(g => g.garmentId);
            existingTypes = [...new Set(garmentsList.map(g => g.garmentType))].sort();
            existingSizes = [...new Set(garmentsList.map(g => g.size))].sort();

            console.log('Processed data:', {
                garmentsCount: garmentsList.length,
                existingTypes: existingTypes,
                existingSizes: existingSizes
            });

            displayGarments();
            updateGarmentDropdown();
            populateDynamicDropdowns();
            populateGarmentFilters();

            // After garments load, check if movements are also loaded and then calculate stats
            if (movementsList.length > 0) {
                calculateAndDisplayStats();
            }
        })
        .catch(err => {
            console.error('Error loading garments:', err);
            document.getElementById('garmentTableBody').innerHTML =
                '<tr><td colspan="6" style="text-align:center;color:#e74c3c;padding:30px;">Failed to load garments</td></tr>';
            garmentsList = [];
            existingTypes = [];
            existingSizes = [];
            calculateAndDisplayStats();
        });
}

function loadMovements() {
    showLoadingState('movementTableBody', 'Loading movements...');
    console.log('Loading movements from API...');

    fetch('/api/garments/movements')
        .then(res => {
            console.log('Movements API response status:', res.status);
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            console.log('Loaded movements:', data);
            movementsList = Array.isArray(data) ? data : [];
            displayMovements();
            populateMovementFilters();

            // After movements load, check if garments are also loaded and then calculate stats
            if (garmentsList.length > 0) {
                calculateAndDisplayStats();
            }
        })
        .catch(err => {
            console.error('Error loading movements:', err);
            document.getElementById('movementTableBody').innerHTML =
                '<tr><td colspan="11" style="text-align:center;color:#e74c3c;padding:30px;">Failed to load movements</td></tr>';
            movementsList = [];
            calculateAndDisplayStats();
        });
}

function showLoadingState(elementId, message = 'Loading...') {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `
            <tr>
                <td colspan="100%" style="text-align:center;padding:30px;color:#666;">
                    <i class="fas fa-spinner fa-spin" style="font-size:24px;"></i><br>${message}
                </td>
            </tr>
        `;
    }
}

function calculateAndDisplayStats() {
    console.log('Calculating stats with:', {
        garmentsCount: garmentsList.length,
        movementsCount: movementsList.length
    });

    const totalTypes = garmentsList.length;
    const totalPieces = garmentsList.reduce((sum, g) => sum + (g.currentStock || g.stock || 0), 0);

    const today = new Date().toISOString().split('T')[0];

    // Calculate produced today from APPROVED movements only
    const producedToday = movementsList
        .filter(m => {
            const matchesDate = m.movementDate === today;
            const isStockIn = m.status === 'In';
            const isApproved = m.approvalStatus === 'APPROVED';
            return matchesDate && isStockIn && isApproved;
        })
        .reduce((sum, m) => sum + parseFloat(m.approvedQuantity || 0), 0);

    // Calculate shipped today from APPROVED movements only
    const shippedToday = movementsList
        .filter(m => {
            const matchesDate = m.movementDate === today;
            const isShipped = m.status === 'Shipped';
            const isApproved = m.approvalStatus === 'APPROVED';
            return matchesDate && isShipped && isApproved;
        })
        .reduce((sum, m) => sum + parseFloat(m.approvedQuantity || 0), 0);

    console.log('Final Stats:', {
        totalTypes,
        totalPieces,
        producedToday,
        shippedToday
    });

    // Always update the DOM, even if values are 0
    document.getElementById('totalGarmentTypes').textContent = totalTypes;
    document.getElementById('totalPieces').textContent = totalPieces;
    document.getElementById('producedToday').textContent = producedToday;
    document.getElementById('shippedToday').textContent = shippedToday;
}

// ============================================
// MODAL FUNCTIONS
// ============================================

function closeGarmentModalFn() {
    document.getElementById('addGarmentModal').style.display = 'none';
    document.getElementById('addGarmentForm').reset();
    document.getElementById('duplicateWarning').style.display = 'none';
    document.getElementById('submitGarmentBtn').disabled = false;
    document.getElementById('submitGarmentBtn').textContent = 'Add Garment';
}

function closeMovementModalFn() {
    document.getElementById('addMovementModal').style.display = 'none';
    document.getElementById('addMovementForm').reset();
    document.getElementById('garmentDetails').innerHTML = 'Select a garment to see details';
    document.getElementById('quantityHelp').style.display = 'block';
    document.getElementById('quantityError').style.display = 'none';
    document.getElementById('movementQuantity').classList.remove('input-error');
    document.getElementById('submitMovementBtn').disabled = false;
    document.getElementById('submitMovementBtn').textContent = 'Record Movement';
    selectedGarmentStock = 0;
}

function closeApprovalModalFn() {
    const modal = document.getElementById('movementApprovalModal');
    if (modal) {
        modal.style.display = 'none';
        document.getElementById('movementApprovalForm')?.reset();
        document.getElementById('rejectReasonGroup').style.display = 'none';
        document.getElementById('submitApprovalBtn').disabled = false;
        document.getElementById('submitApprovalBtn').textContent = 'Submit Decision';
    }
}

function setTodayDate() {
    const today = new Date().toISOString().split('T')[0];
    const movementDate = document.getElementById('movementDate');
    const decisionDate = document.getElementById('decisionDate');

    if (movementDate) {
        movementDate.value = today;
        movementDate.min = today;
        movementDate.max = today;
    }

    if (decisionDate) {
        decisionDate.value = today;
    }
}

function toggleRejectReason() {
    const rejectedQty = document.getElementById('rejectedQuantity');
    const reasonGroup = document.getElementById('rejectReasonGroup');

    if (rejectedQty && reasonGroup) {
        reasonGroup.style.display = parseFloat(rejectedQty.value) > 0 ? 'block' : 'none';
    }
}

function updateRejectedQuantity() {
    const approvedQty = parseFloat(document.getElementById('approvedQuantity').value) || 0;
    const totalQty = parseFloat(document.getElementById('approvalTotalQuantity').value) || 0;
    const rejectedQty = document.getElementById('rejectedQuantity');

    if (rejectedQty && approvedQty <= totalQty) {
        const calculatedRejected = totalQty - approvedQty;
        rejectedQty.value = calculatedRejected > 0 ? calculatedRejected : 0;
        toggleRejectReason();
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

    if (typeFilter) filteredGarments = filteredGarments.filter(g => g.garmentType === typeFilter);
    if (sizeFilter) filteredGarments = filteredGarments.filter(g => g.size === sizeFilter);

    if (sortValue === 'stock-desc') {
        filteredGarments.sort((a, b) => (b.currentStock || b.stock || 0) - (a.currentStock || a.stock || 0));
    } else if (sortValue === 'stock-asc') {
        filteredGarments.sort((a, b) => (a.currentStock || a.stock || 0) - (b.currentStock || b.stock || 0));
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
            const stock = g.currentStock || 0;
            const garmentType = g.garmentType || g.type || 'N/A';

            const stockClass = stock <= 10 ? 'style="color: #e74c3c; font-weight: bold;"' : '';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${g.garmentId}</td>
                <td>${garmentType}</td>
                <td>${g.size}</td>
                <td>${g.fabricId}</td>
                <td ${stockClass}>${stock}</td>
                <td class="action-buttons-table">
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
            return garment && (garment.garmentType === typeFilter || garment.type === typeFilter);
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
            const statusClass = m.status === 'In' ? 'status-in' : 'status-shipped';
            const statusText = m.status === 'In' ? 'Stock In' : 'Shipped';

            // Approval status display
            let approvalInfo = '<span style="color:#f39c12;">Pending</span>';
            let approvedQty = 'N/A';
            let rejectedQty = 'N/A';
            let rejectReason = 'N/A';

            // Check if movement is approved
            if (m.approvalStatus === 'APPROVED') {
                approvalInfo = `<span style="color:#27ae60;">Approved</span>`;
                approvedQty = m.approvedQuantity ? m.approvedQuantity : '0';
                rejectedQty = m.rejectedQuantity ? m.rejectedQuantity : '0';
                rejectReason = m.rejectionReason || 'N/A';
            }

            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${m.movementId}</td>
                <td>${m.garmentId}</td>
                <td>${garment ? garment.fabricId : 'N/A'}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${m.movementDate}</td>
                <td>${m.quantity || 0}</td>
                <td class="approval-details">${approvalInfo}</td>
                <td>${approvedQty}</td>
                <td>${rejectedQty}</td>
                <td>${rejectReason}</td>
                <td class="action-buttons-table">
                    ${m.approvalStatus !== 'APPROVED' ?
                `<button class="btn add approve-btn" data-movement-id="${m.movementId}" data-garment-id="${m.garmentId}" data-quantity="${m.quantity}" data-status="${m.status}">
                            <i class="fas fa-check"></i> Approve
                        </button>` : ''}
                    <button class="btn delete btn-delete-movement" data-id="${m.movementId}">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        tbody.innerHTML = '<tr><td colspan="11" style="text-align:center;color:#666;padding:30px;">No movements match the selected filters</td></tr>';
    }
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

    const types = [...new Set(garmentsList.map(g => g.garmentType || g.type))].sort();

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
        const stock = g.currentStock || g.stock || 0;
        const garmentType = g.garmentType || g.type || 'N/A';

        const option = document.createElement('option');
        option.value = g.garmentId;
        option.textContent = `${g.garmentId} - ${garmentType} (${g.size}) - Stock: ${stock}pcs`;
        option.dataset.type = garmentType;
        option.dataset.size = g.size;
        option.dataset.fabric = g.fabricId;
        option.dataset.stock = stock;
        select.appendChild(option);
    });
}

function populateDynamicDropdowns() {
    console.log('Populating dynamic dropdowns...');
    populateTypeDropdown();
    populateSizeDropdown();
    loadFabricsForDropdown();
}

function populateTypeDropdown() {
    const typeSelect = document.getElementById('newGarmentType');
    if (!typeSelect) {
        console.error('newGarmentType element not found!');
        return;
    }

    console.log('Populating type dropdown, existingTypes:', existingTypes);

    const currentValue = typeSelect.value;
    typeSelect.innerHTML = '';

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select Garment Type';
    typeSelect.appendChild(defaultOption);

    // Add existing types
    if (existingTypes && existingTypes.length > 0) {
        existingTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = type;
            typeSelect.appendChild(option);
        });
    } else {
        console.log('No existing types found, adding default options');
        // Add some default garment types if none exist
        const defaultTypes = ['T-Shirt', 'Shirt', 'Pants', 'Dress', 'Jacket', 'Skirt'];
        defaultTypes.forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = type;
            typeSelect.appendChild(option);
        });
    }

    const separator = document.createElement('option');
    separator.disabled = true;
    separator.textContent = '──────────';
    typeSelect.appendChild(separator);

    const addNewOption = document.createElement('option');
    addNewOption.value = '_new_';
    addNewOption.textContent = '+ Add New Type...';
    addNewOption.style.fontWeight = 'bold';
    addNewOption.style.color = '#4b0082';
    typeSelect.appendChild(addNewOption);

    if (currentValue && currentValue !== '_new_') {
        typeSelect.value = currentValue;
    }

    console.log('Type dropdown populated with options:', Array.from(typeSelect.options).map(o => o.value));

    // Remove existing event listeners and add new one
    typeSelect.removeEventListener('change', handleTypeChange);
    typeSelect.addEventListener('change', handleTypeChange);
}

function handleTypeChange(e) {
    const typeSelect = e.target;
    if (typeSelect.value === '_new_') {
        setTimeout(() => {
            const newType = prompt('Enter new garment type:');
            if (newType && newType.trim()) {
                const trimmedType = newType.trim();

                const option = document.createElement('option');
                option.value = trimmedType;
                option.textContent = trimmedType;

                const separator = typeSelect.querySelector('option[disabled]');
                typeSelect.insertBefore(option, separator);
                typeSelect.value = trimmedType;
            } else {
                typeSelect.value = '';
            }
            checkDuplicateTypeSizeFabric();
        }, 100);
    } else {
        checkDuplicateTypeSizeFabric();
    }
}

function populateSizeDropdown() {
    const sizeSelect = document.getElementById('newGarmentSize');
    if (!sizeSelect) {
        console.error('newGarmentSize element not found!');
        return;
    }

    const currentValue = sizeSelect.value;
    sizeSelect.innerHTML = '';

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select Size';
    sizeSelect.appendChild(defaultOption);

    // Standard garment sizes
    const standardSizes = ['XS', 'S', 'M', 'L', 'XL'];

    // Add existing sizes first, then standard sizes
    const allSizes = [...new Set([...existingSizes, ...standardSizes])].sort();

    allSizes.forEach(size => {
        const option = document.createElement('option');
        option.value = size;
        option.textContent = size;
        sizeSelect.appendChild(option);
    });

    const separator = document.createElement('option');
    separator.disabled = true;
    separator.textContent = '──────────';
    sizeSelect.appendChild(separator);

    const addNewOption = document.createElement('option');
    addNewOption.value = '_new_';
    addNewOption.textContent = '+ Add New Size...';
    addNewOption.style.fontWeight = 'bold';
    addNewOption.style.color = '#4b0082';
    sizeSelect.appendChild(addNewOption);

    if (currentValue && currentValue !== '_new_') {
        sizeSelect.value = currentValue;
    }

    console.log('Size dropdown populated with options:', Array.from(sizeSelect.options).map(o => o.value));

    sizeSelect.removeEventListener('change', handleSizeChange);
    sizeSelect.addEventListener('change', handleSizeChange);
}

function handleSizeChange(e) {
    const sizeSelect = e.target;
    if (sizeSelect.value === '_new_') {
        setTimeout(() => {
            const newSize = prompt('Enter new size:');
            if (newSize && newSize.trim()) {
                const trimmedSize = newSize.trim().toUpperCase();

                const option = document.createElement('option');
                option.value = trimmedSize;
                option.textContent = trimmedSize;

                const separator = sizeSelect.querySelector('option[disabled]');
                sizeSelect.insertBefore(option, separator);
                sizeSelect.value = trimmedSize;
            } else {
                sizeSelect.value = '';
            }
            checkDuplicateTypeSizeFabric();
        }, 100);
    } else {
        checkDuplicateTypeSizeFabric();
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
            if (fabricSelect) {
                fabricSelect.innerHTML = '<option value="">Select Fabric ID</option>';
                fabrics.forEach(f => {
                    const option = document.createElement('option');
                    option.value = f.fabricId;
                    option.textContent = `${f.fabricId} - ${f.fabricType} (${f.color})`;
                    fabricSelect.appendChild(option);
                });
                console.log('Fabric dropdown populated with', fabrics.length, 'options');
            }
        })
        .catch(err => {
            console.error('Error loading fabrics:', err);
        });
}

// ============================================
// VALIDATION FUNCTIONS
// ============================================

function checkDuplicateTypeSizeFabric() {
    const typeSelect = document.getElementById('newGarmentType');
    const sizeSelect = document.getElementById('newGarmentSize');
    const fabricSelect = document.getElementById('newGarmentFabricId');
    const warningDiv = document.getElementById('duplicateWarning');

    if (!typeSelect || !sizeSelect || !fabricSelect || !warningDiv) return false;

    // Don't check if any field is empty or has placeholder values
    if (!typeSelect.value || !sizeSelect.value || !fabricSelect.value ||
        typeSelect.value === '_new_' || sizeSelect.value === '_new_') {
        warningDiv.style.display = 'none';
        return false;
    }

    const isDuplicate = garmentsList.some(g =>
        (g.garmentType === typeSelect.value || g.type === typeSelect.value) &&
        g.size === sizeSelect.value &&
        g.fabricId === fabricSelect.value
    );

    console.log('Duplicate check:', {
        type: typeSelect.value,
        size: sizeSelect.value,
        fabric: fabricSelect.value,
        isDuplicate: isDuplicate
    });

    if (isDuplicate) {
        warningDiv.style.display = 'block';
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

        let stockWarning = '';
        if (stock < 10) {
            stockWarning = '<br><span style="color:#e74c3c;">⚠ Low Stock Warning: Below 10 pieces threshold</span>';
        } else if (stock === 10) {
            stockWarning = '<br><span style="color:#f39c12;">⚠ Critical Stock: At 10 pieces threshold</span>';
        }

        detailsDiv.innerHTML = `
            <strong>Garment ID:</strong> ${select.value}<br>
            <strong>Type:</strong> ${selectedOption.dataset.type}<br>
            <strong>Size:</strong> ${selectedOption.dataset.size}<br>
            <strong>Fabric ID:</strong> ${selectedOption.dataset.fabric}<br>
            <strong>Current Stock:</strong> ${stock} pieces${stockWarning}
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
        errorText.textContent = 'Quantity must be greater than 0';
        errorText.style.display = 'block';
        input.classList.add('input-error');
        return false;
    }

    if (statusSelect.value === 'Shipped' && quantity > selectedGarmentStock) {
        helpText.style.display = 'none';
        errorText.textContent = 'Quantity exceeds available stock!';
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
        showNotification(`Quantity (${quantity}pcs) exceeds available stock (${selectedGarmentStock}pcs)!`, 'error');
        quantityInput?.focus();
        return false;
    }

    return true;
}

function validateGarmentForm() {
    const typeSelect = document.getElementById('newGarmentType');
    const sizeSelect = document.getElementById('newGarmentSize');
    const fabricSelect = document.getElementById('newGarmentFabricId');
    const initialStock = document.getElementById('initialStock');

    console.log('Form validation - Values:', {
        type: typeSelect.value,
        size: sizeSelect.value,
        fabric: fabricSelect.value,
        stock: initialStock.value
    });

    if (!typeSelect.value || typeSelect.value === '_new_') {
        showNotification('Please select a valid garment type', 'error');
        typeSelect.focus();
        return false;
    }

    if (!sizeSelect.value || sizeSelect.value === '_new_') {
        showNotification('Please select a valid size', 'error');
        sizeSelect.focus();
        return false;
    }

    if (!fabricSelect.value) {
        showNotification('Please select a fabric', 'error');
        fabricSelect.focus();
        return false;
    }

    const stock = parseInt(initialStock.value);
    if (isNaN(stock) || stock < 10) {
        showNotification('Initial stock must be at least 10 pieces', 'error');
        initialStock.focus();
        return false;
    }

    if (checkDuplicateTypeSizeFabric()) {
        showNotification('This type/size/fabric combination already exists', 'error');
        return false;
    }

    console.log('Form validation passed');
    return true;
}

function validateApprovalForm() {
    const approvedQty = parseFloat(document.getElementById('approvedQuantity').value);
    const totalQty = parseFloat(document.getElementById('approvalTotalQuantity').value);
    const rejectedQty = parseFloat(document.getElementById('rejectedQuantity').value) || 0;

    if (isNaN(approvedQty) || approvedQty < 0) {
        showNotification('Please enter a valid approved quantity', 'error');
        return false;
    }

    if (approvedQty + rejectedQty !== totalQty) {
        showNotification(`Approved (${approvedQty}) + Rejected (${rejectedQty}) must equal Total (${totalQty})`, 'error');
        return false;
    }

    if (rejectedQty > 0) {
        const rejectReason = document.getElementById('rejectReason').value.trim();
        if (!rejectReason) {
            showNotification('Please provide a reason for rejection', 'error');
            return false;
        }
    }

    return true;
}

// ============================================
// FORM SUBMISSION HANDLERS
// ============================================

function handleGarmentSubmit(e) {
    e.preventDefault();
    console.log('Garment form submitted');

    if (!validateGarmentForm()) {
        console.log('Garment form validation failed');
        return;
    }

    const form = e.target;
    const garmentId = form.garmentId.value.trim().toUpperCase();
    const fabricId = form.fabricId.value;
    const garmentType = form.garmentType.value.trim();
    const size = form.size.value;
    const initialStock = parseInt(form.initialStock.value);

    // Create GarmentCreationRequest with product details
    const data = {
        garmentType: garmentType,
        size: size,
        fabricId: fabricId,
        initialStock: initialStock,
        name: generateProductName(garmentType, size, fabricId),
        description: generateProductDescription(garmentType, size, fabricId),
        price: calculateDefaultPrice(garmentType, size),
        category: "Apparel"
    };

    console.log('Submitting garment with product data:', data);

    const submitBtn = document.getElementById('submitGarmentBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Adding...';

    // Use the combined endpoint that creates both garment and product
    fetch('/api/inventory/garments/create-with-product', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => {
            console.log('Response status:', res.status);
            if (!res.ok) {
                return res.text().then(text => {
                    console.log('Error response:', text);
                    throw new Error(`HTTP ${res.status}: ${text}`);
                });
            }
            return res.json();
        })
        .then(resp => {
            console.log('Garment with product creation response:', resp);
            if (resp && resp.garmentId) {
                showNotification(`Garment ${resp.garmentId} and corresponding product created successfully!`, 'success');
                closeGarmentModalFn();

                // Update local arrays
                if (!existingTypes.includes(garmentType)) {
                    existingTypes.push(garmentType);
                    existingTypes.sort();
                }
                if (!existingSizes.includes(size)) {
                    existingSizes.push(size);
                    existingSizes.sort();
                }

                refreshAllData();
            } else {
                throw new Error('Invalid response from server - no garmentId returned');
            }
        })
        .catch(err => {
            console.error('Garment creation error:', err);
            showNotification('Error: ' + err.message, 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Add Garment';
        });
}

// Helper functions to generate product details
function generateProductName(type, size, fabricId) {
    return type + " - " + size + " (" + fabricId + ")";
}

function generateProductDescription(type, size, fabricId) {
    return "High quality " + type.toLowerCase() + " in size " + size +
        " made from fabric " + fabricId + ". Comfortable and durable.";
}

function calculateDefaultPrice(type, size) {
    // Set default price based on garment type/size
    const priceMap = {
        "T-Shirt": 25.0,
        "Shirt": 35.0,
        "Pants": 45.0,
        "Dress": 60.0,
        "Jacket": 80.0
    };

    const basePrice = priceMap[type] || 30.0;

    // Adjust for size if needed
    const sizeMultiplier = {
        "S": 1.0,
        "M": 1.1,
        "L": 1.2,
        "XL": 1.3
    };

    const multiplier = sizeMultiplier[size] || 1.0;
    return basePrice * multiplier;
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

    const data = {
        garmentId: garmentId,
        status: status,
        movementDate: form.movementDate.value,
        quantity: quantity
    };

    const submitBtn = document.getElementById('submitMovementBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Recording...';
    }

    fetch('/api/garments/movements', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
            return res.json();
        })
        .then(resp => {
            if (resp && resp.movementId) {
                showNotification(`Movement ${resp.movementId} recorded successfully!`, 'success');
                closeMovementModalFn();
                refreshAllData();
            } else {
                showNotification('Error recording movement', 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = 'Record Movement';
                }
            }
        })
        .catch(err => {
            console.error(err);
            showNotification('Server error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Record Movement';
            }
        });
}

function handleApprovalSubmit(e) {
    e.preventDefault();

    if (!validateApprovalForm()) {
        return;
    }

    const form = e.target;
    const movementId = form.movementId.value;
    const garmentId = document.getElementById('approvalGarmentId').value;
    const approvedQuantity = parseFloat(form.approvedQuantity.value);
    const rejectedQuantity = parseFloat(form.rejectedQuantity.value) || 0;
    const rejectReason = form.rejectReason.value.trim();

    const data = {
        movementId: movementId,
        garmentId: garmentId,
        approvedQuantity: approvedQuantity,
        rejectedQuantity: rejectedQuantity,
        rejectionReason: rejectReason || null
    };

    const submitBtn = document.getElementById('submitApprovalBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Submitting...';
    }

    fetch('/api/garments/movements/approve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(`HTTP ${res.status}: ${text}`);
                });
            }
            return res.json();
        })
        .then(resp => {
            if (resp) {
                showNotification('Movement approval recorded successfully!', 'success');
                closeApprovalModalFn();
                refreshAllData();
            } else {
                throw new Error('Invalid response from server');
            }
        })
        .catch(err => {
            console.error('Approval error:', err);
            showNotification('Error: ' + err.message, 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Submit Decision';
            }
        });
}

// ============================================
// ACTION HANDLERS
// ============================================

function handleActions(e) {
    const deleteGarmentBtn = e.target.closest('.btn-delete-garment');
    const deleteMovementBtn = e.target.closest('.btn-delete-movement');
    const approveBtn = e.target.closest('.approve-btn');

    if (deleteGarmentBtn) {
        const id = deleteGarmentBtn.dataset.id;
        showConfirmDialog(
            `Are you sure you want to delete garment <strong>${id}</strong>?<br><br>This will also delete all associated movements and cannot be undone.`,
            () => {
                deleteGarmentBtn.disabled = true;
                deleteGarmentBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

                fetch(`/api/garments/${id}`, {
                    method: 'DELETE'
                })
                    .then(res => {
                        console.log('Delete garment response status:', res.status);
                        if (res.status === 200) {
                            return res.text();
                        } else if (res.status === 404) {
                            throw new Error('Garment not found');
                        } else {
                            throw new Error(`HTTP error! Status: ${res.status}`);
                        }
                    })
                    .then(responseText => {
                        console.log('Delete garment response:', responseText);
                        showNotification('Garment deleted successfully', 'success');
                        refreshAllData();
                    })
                    .catch(err => {
                        console.error('Delete garment error:', err);
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
                deleteMovementBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

                fetch(`/api/garments/movements/${id}`, {
                    method: 'DELETE'
                })
                    .then(res => {
                        console.log('Delete movement response status:', res.status);
                        if (res.status === 200) {
                            return res.text();
                        } else if (res.status === 404) {
                            throw new Error('Movement not found');
                        } else {
                            throw new Error(`HTTP error! Status: ${res.status}`);
                        }
                    })
                    .then(responseText => {
                        console.log('Delete movement response:', responseText);
                        showNotification('Movement deleted successfully', 'success');
                        refreshAllData();
                    })
                    .catch(err => {
                        console.error('Delete movement error:', err);
                        showNotification('Error deleting movement: ' + err.message, 'error');
                        deleteMovementBtn.disabled = false;
                        deleteMovementBtn.innerHTML = '<i class="fas fa-trash"></i> Delete';
                    });
            }
        );
    }

    if (approveBtn) {
        const movementId = approveBtn.dataset.movementId;
        const garmentId = approveBtn.dataset.garmentId;
        const quantity = parseFloat(approveBtn.dataset.quantity);

        document.getElementById('approvalMovementId').value = movementId;
        document.getElementById('approvalGarmentId').value = garmentId;
        document.getElementById('approvalTotalQuantity').value = quantity;
        document.getElementById('approvedQuantity').value = quantity;
        document.getElementById('rejectedQuantity').value = '0';
        document.getElementById('rejectReason').value = '';
        document.getElementById('rejectReasonGroup').style.display = 'none';
        document.getElementById('movementApprovalModal').style.display = 'block';
        setTodayDate();
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

function refreshAllData() {
    console.log('Refreshing all data...');

    Promise.all([
        loadGarments(),
        loadMovements()
    ]).then(() => {
        console.log('Both datasets refreshed, updating stats...');
        calculateAndDisplayStats();
    }).catch(err => {
        console.error('Error refreshing data:', err);
        calculateAndDisplayStats();
    });
}

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
                <h3 class="modal-title"><i class="fas fa-exclamation-triangle"></i> Confirm Action</h3>
            </div>
            <div style="padding:25px;">
                <p style="margin-bottom:20px;color:#555;line-height:1.6;">${message}</p>
                <div class="form-buttons">
                    <button type="button" class="btn btn-cancel" id="confirmCancel">Cancel</button>
                    <button type="button" class="btn delete" id="confirmOk">Confirm</button>
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

    const n = document.createElement('div');
    n.className = `notification notification-${type}`;
    n.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${iconMap[type] || 'info-circle'}"></i>
            <span>${msg}</span>
        </div>
    `;

    document.body.appendChild(n);

    setTimeout(() => n.classList.add('show'), 100);
    setTimeout(() => {
        n.classList.remove('show');
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

// ============================================
// ASYNC DATA LOADING FUNCTIONS
// ============================================

    function loadGarmentsAsync() {
        return new Promise((resolve, reject) => {
            showLoadingState('garmentTableBody', 'Loading garments...');
            console.log('Loading garments from API...');

            fetch('/api/garments')
                .then(res => {
                    console.log('Garments API response status:', res.status);
                    if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                    return res.json();
                })
                .then(data => {
                    console.log('Loaded garments:', data);
                    // Map backend properties to frontend expected properties
                    garmentsList = Array.isArray(data) ? data.map(g => {
                        return {
                            ...g,
                            garmentType: g.type, // Map 'type' to 'garmentType' for frontend
                            currentStock: g.currentStock || 0
                        };
                    }) : [];

                    existingGarmentIds = garmentsList.map(g => g.garmentId);
                    existingTypes = [...new Set(garmentsList.map(g => g.garmentType))].sort();
                    existingSizes = [...new Set(garmentsList.map(g => g.size))].sort();

                    console.log('Processed data:', {
                        garmentsCount: garmentsList.length,
                        existingTypes: existingTypes,
                        existingSizes: existingSizes
                    });

                    displayGarments();
                    updateGarmentDropdown();
                    populateDynamicDropdowns();
                    populateGarmentFilters();
                    resolve();
                })
                .catch(err => {
                    console.error('Error loading garments:', err);
                    document.getElementById('garmentTableBody').innerHTML =
                        '<tr><td colspan="6" style="text-align:center;color:#e74c3c;padding:30px;">Failed to load garments</td></tr>';
                    garmentsList = [];
                    existingTypes = [];
                    existingSizes = [];
                    reject(err);
                });
        });
    }

    function loadMovementsAsync() {
        return new Promise((resolve, reject) => {
            showLoadingState('movementTableBody', 'Loading movements...');
            console.log('Loading movements from API...');

            fetch('/api/garments/movements')
                .then(res => {
                    console.log('Movements API response status:', res.status);
                    if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
                    return res.json();
                })
                .then(data => {
                    console.log('Loaded movements:', data);
                    movementsList = Array.isArray(data) ? data : [];
                    displayMovements();
                    populateMovementFilters();
                    resolve();
                })
                .catch(err => {
                    console.error('Error loading movements:', err);
                    document.getElementById('movementTableBody').innerHTML =
                        '<tr><td colspan="11" style="text-align:center;color:#e74c3c;padding:30px;">Failed to load movements</td></tr>';
                    movementsList = [];
                    reject(err);
                });
        });
    }});