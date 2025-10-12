// Mobile menu functionality
const menuToggle = document.getElementById('menuToggle');
const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');

if (menuToggle && sidebar && sidebarOverlay) {
    menuToggle.addEventListener('click', function() {
        sidebar.classList.toggle('open');
        sidebarOverlay.classList.toggle('show');
    });
}

if (sidebarOverlay) {
    sidebarOverlay.addEventListener('click', function() {
        sidebar.classList.remove('open');
        sidebarOverlay.classList.remove('show');
    });
}

const navItems = document.querySelectorAll('.nav-item');
navItems.forEach(item => {
    item.addEventListener('click', function() {
        if (window.innerWidth <= 768) {
            sidebar.classList.remove('open');
            sidebarOverlay.classList.remove('show');
        }
    });
});

// Chart variables
let fabricMovementChart, garmentMovementChart;

// Initialize charts
function initializeCharts() {
    console.log('Initializing charts...');

    // Fabric Movement Chart
    const fabricCtx = document.getElementById('fabricMovementChart');
    if (fabricCtx) {
        fabricMovementChart = new Chart(fabricCtx.getContext('2d'), {
            type: 'line',
            data: {
                labels: getLast7Days(),
                datasets: [
                    {
                        label: 'Stock IN (meters)',
                        data: new Array(7).fill(0),
                        borderColor: '#27ae60',
                        backgroundColor: 'rgba(39, 174, 96, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Stock OUT (meters)',
                        data: new Array(7).fill(0),
                        borderColor: '#e74c3c',
                        backgroundColor: 'rgba(231, 76, 60, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: 'Fabric IN vs OUT (Last 7 Days)'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.1)' },
                        title: {
                            display: true,
                            text: 'Quantity (meters)'
                        }
                    },
                    x: {
                        grid: { color: 'rgba(0,0,0,0.1)' }
                    }
                }
            }
        });
    } else {
        console.error('Fabric chart canvas element not found');
    }

    // Garment Movement Chart - UPDATED to IN vs REJECT
    const garmentCtx = document.getElementById('garmentMovementChart');
    if (garmentCtx) {
        garmentMovementChart = new Chart(garmentCtx.getContext('2d'), {
            type: 'line',
            data: {
                labels: getLast7Days(),
                datasets: [
                    {
                        label: 'Stock IN (pieces)',
                        data: new Array(7).fill(0),
                        borderColor: '#3498db',
                        backgroundColor: 'rgba(52, 152, 219, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Rejected (pieces)',
                        data: new Array(7).fill(0),
                        borderColor: '#e74c3c',
                        backgroundColor: 'rgba(231, 76, 60, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: 'Garment IN vs REJECT (Last 7 Days)'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.1)' },
                        title: {
                            display: true,
                            text: 'Quantity (pieces)'
                        }
                    },
                    x: {
                        grid: { color: 'rgba(0,0,0,0.1)' }
                    }
                }
            }
        });
    } else {
        console.error('Garment chart canvas element not found');
    }

    console.log('Charts initialized successfully');
}

// Get last 7 days labels
function getLast7Days() {
    const days = [];
    const today = new Date();
    for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        days.push(date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }));
    }
    return days;
}

// Load all dashboard data
function loadDashboardData() {
    console.log('Loading dashboard data...');
    loadTotalStats();
    loadFabricMovementChart();
    loadGarmentMovementChart();
    loadFabricLowStockAlerts();
    loadGarmentLowStockAlerts();
}

// Load total statistics - UPDATED
function loadTotalStats() {
    console.log('Loading dashboard stats...');
    fetch('/api/dashboard/stats')
        .then(res => {
            console.log('Stats response status:', res.status);
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(stats => {
            console.log('Dashboard stats received:', stats);
            document.getElementById('totalFabricsInStock').textContent = stats.totalFabrics + 'm';
            document.getElementById('totalGarmentsInStock').textContent = stats.totalGarments + 'pcs';
            document.getElementById('totalRejectedFabrics').textContent = stats.rejectedFabrics;
            document.getElementById('totalRejectedGarments').textContent = stats.rejectedGarments;
        })
        .catch(err => {
            console.error('Error loading dashboard stats:', err);
            // Set default values
            document.getElementById('totalFabricsInStock').textContent = '0m';
            document.getElementById('totalGarmentsInStock').textContent = '0pcs';
            document.getElementById('totalRejectedFabrics').textContent = '0';
            document.getElementById('totalRejectedGarments').textContent = '0';
        });
}

// Load fabric movement chart - UPDATED
function loadFabricMovementChart() {
    console.log('Loading fabric movements...');
    fetch('/api/dashboard/fabric-movements')
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(movements => {
            console.log('Fabric movements received:', movements);
            const labels = getLast7Days();
            const stockIn = new Array(7).fill(0);
            const stockOut = new Array(7).fill(0);

            movements.forEach(m => {
                const movementDate = new Date(m.movement_date);
                const today = new Date();
                const diffDays = Math.floor((today - movementDate) / (1000 * 60 * 60 * 24));

                if (diffDays >= 0 && diffDays < 7) {
                    const index = 6 - diffDays;
                    const quantity = m.total_quantity || 0;

                    if (m.status === 'In') {
                        stockIn[index] += quantity;
                    } else if (m.status === 'Out') {
                        stockOut[index] += quantity;
                    }
                }
            });

            if (fabricMovementChart) {
                fabricMovementChart.data.labels = labels;
                fabricMovementChart.data.datasets[0].data = stockIn;
                fabricMovementChart.data.datasets[1].data = stockOut;
                fabricMovementChart.update();
            }
        })
        .catch(err => {
            console.error('Error loading fabric movements:', err);
            // Set empty data
            const labels = getLast7Days();
            if (fabricMovementChart) {
                fabricMovementChart.data.labels = labels;
                fabricMovementChart.data.datasets[0].data = new Array(7).fill(0);
                fabricMovementChart.data.datasets[1].data = new Array(7).fill(0);
                fabricMovementChart.update();
            }
        });
}

// Load garment movement chart - UPDATED to show IN vs REJECT
function loadGarmentMovementChart() {
    console.log('Loading garment movements...');
    fetch('/api/dashboard/garment-movements')
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(movements => {
            console.log('Garment movements received:', movements);
            const labels = getLast7Days();
            const stockIn = new Array(7).fill(0);
            const rejected = new Array(7).fill(0);

            movements.forEach(m => {
                const movementDate = new Date(m.movement_date);
                const today = new Date();
                const diffDays = Math.floor((today - movementDate) / (1000 * 60 * 60 * 24));

                if (diffDays >= 0 && diffDays < 7) {
                    const index = 6 - diffDays;
                    const quantity = m.total_quantity || 0;
                    const rejectedQty = m.rejected_quantity || 0;

                    if (m.status === 'In') {
                        stockIn[index] += quantity;
                    }
                    // Count rejected items regardless of status
                    if (rejectedQty > 0) {
                        rejected[index] += rejectedQty;
                    }
                    // Alternative: check approval_status
                    if (m.approval_status === 'REJECTED') {
                        rejected[index] += quantity;
                    }
                }
            });

            if (garmentMovementChart) {
                garmentMovementChart.data.labels = labels;
                garmentMovementChart.data.datasets[0].data = stockIn;
                garmentMovementChart.data.datasets[1].data = rejected;
                garmentMovementChart.update();
            }
        })
        .catch(err => {
            console.error('Error loading garment movements:', err);
            // Set empty data
            const labels = getLast7Days();
            if (garmentMovementChart) {
                garmentMovementChart.data.labels = labels;
                garmentMovementChart.data.datasets[0].data = new Array(7).fill(0);
                garmentMovementChart.data.datasets[1].data = new Array(7).fill(0);
                garmentMovementChart.update();
            }
        });
}

// Load fabric low stock alerts - UPDATED
function loadFabricLowStockAlerts() {
    console.log('Loading fabric alerts...');
    fetch('/api/dashboard/low-stock/fabrics')
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            console.log('Fabric alerts received:', data);
            const container = document.getElementById('fabricLowStockList');

            if (data && data.length > 0) {
                container.innerHTML = '';
                data.forEach(item => {
                    const currentStock = item.current_stock || 0;
                    const reorderLevel = item.reorder_level || 0;
                    const isCritical = currentStock < (reorderLevel * 0.3);

                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'activity-item';
                    alertDiv.innerHTML = `
                        <div class="activity-icon ${isCritical ? 'critical' : 'warning'}">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <div class="activity-content">
                            <h5>
                                Fabric ${item.type} ${item.color} is below reorder level.
                                ${isCritical ? '<span class="alert-badge critical">CRITICAL</span>' : '<span class="alert-badge warning">LOW</span>'}
                            </h5>
                            <p><strong>Fabric ID:</strong> ${item.fabric_id}</p>
                            <p><strong>Current Stock:</strong> ${currentStock}m (Reorder level: ${reorderLevel}m)</p>
                        </div>
                    `;
                    container.appendChild(alertDiv);
                });
            } else {
                container.innerHTML = '<p style="text-align: center; color: #27ae60; padding: 20px;"><i class="fas fa-check-circle"></i> All fabric stock levels are healthy!</p>';
            }
        })
        .catch(err => {
            console.error('Error loading fabric alerts:', err);
            document.getElementById('fabricLowStockList').innerHTML = '<p style="text-align: center; color: #e74c3c; padding: 20px;">Error loading alerts</p>';
        });
}

// Load garment low stock alerts - UPDATED
function loadGarmentLowStockAlerts() {
    console.log('Loading garment alerts...');
    fetch('/api/dashboard/low-stock/garments')
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            console.log('Garment alerts received:', data);
            const container = document.getElementById('garmentLowStockList');

            if (data && data.length > 0) {
                container.innerHTML = '';
                data.forEach(item => {
                    const currentStock = item.current_stock || 0;
                    const reorderLevel = item.reorder_level || 0;
                    const isCritical = currentStock < (reorderLevel * 0.3);

                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'activity-item';
                    alertDiv.innerHTML = `
                        <div class="activity-icon ${isCritical ? 'critical' : 'warning'}">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <div class="activity-content">
                            <h5>
                                Garment ${item.type} (${item.size}) is below reorder level.
                                ${isCritical ? '<span class="alert-badge critical">CRITICAL</span>' : '<span class="alert-badge warning">LOW</span>'}
                            </h5>
                            <p><strong>Garment ID:</strong> ${item.garment_id}</p>
                            <p><strong>Current Stock:</strong> ${currentStock}pcs (Reorder level: ${reorderLevel}pcs)</p>
                        </div>
                    `;
                    container.appendChild(alertDiv);
                });
            } else {
                container.innerHTML = '<p style="text-align: center; color: #27ae60; padding: 20px;"><i class="fas fa-check-circle"></i> All garment stock levels are healthy!</p>';
            }
        })
        .catch(err => {
            console.error('Error loading garment alerts:', err);
            document.getElementById('garmentLowStockList').innerHTML = '<p style="text-align: center; color: #e74c3c; padding: 20px;">Error loading alerts</p>';
        });
}

// Auto-refresh dashboard data every 5 minutes
setInterval(loadDashboardData, 300000);

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing dashboard...');
    initializeCharts();
    loadDashboardData();
});

// Responsive chart resize
window.addEventListener('resize', function() {
    if (fabricMovementChart) fabricMovementChart.resize();
    if (garmentMovementChart) garmentMovementChart.resize();
});