// Mobile menu functionality
const menuToggle = document.getElementById('menuToggle');
const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');

menuToggle.addEventListener('click', function() {
    sidebar.classList.toggle('open');
    sidebarOverlay.classList.toggle('show');
});

sidebarOverlay.addEventListener('click', function() {
    sidebar.classList.remove('open');
    sidebarOverlay.classList.remove('show');
});

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
    // Fabric Movement Chart
    const fabricCtx = document.getElementById('fabricMovementChart').getContext('2d');
    fabricMovementChart = new Chart(fabricCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Stock IN (meters)',
                    data: [],
                    borderColor: '#27ae60',
                    backgroundColor: 'rgba(39, 174, 96, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Stock OUT (meters)',
                    data: [],
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
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,0.1)' }
                },
                x: {
                    grid: { color: 'rgba(0,0,0,0.1)' }
                }
            }
        }
    });

    // Garment Movement Chart
    const garmentCtx = document.getElementById('garmentMovementChart').getContext('2d');
    garmentMovementChart = new Chart(garmentCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Stock IN (pieces)',
                    data: [],
                    borderColor: '#3498db',
                    backgroundColor: 'rgba(52, 152, 219, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Shipped (pieces)',
                    data: [],
                    borderColor: '#f39c12',
                    backgroundColor: 'rgba(243, 156, 18, 0.1)',
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
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,0.1)' }
                },
                x: {
                    grid: { color: 'rgba(0,0,0,0.1)' }
                }
            }
        }
    });
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
    loadTotalStats();
    loadFabricMovementChart();
    loadGarmentMovementChart();
    loadFabricLowStockAlerts();
    loadGarmentLowStockAlerts();
}

// Load total statistics
function loadTotalStats() {
    // Load fabric total
    fetch('/api/fabrics/all')
        .then(res => res.json())
        .then(data => {
            const totalMeters = data.reduce((sum, f) => sum + (f.currentStock || 0), 0);
            document.getElementById('totalFabricsInStock').textContent = totalMeters + 'm';
        })
        .catch(err => console.error('Error loading fabric stats:', err));

    // Load garment total
    fetch('/api/garments/all')
        .then(res => res.json())
        .then(data => {
            const totalPieces = data.reduce((sum, g) => sum + (g.currentStock || 0), 0);
            document.getElementById('totalGarmentsInStock').textContent = totalPieces + 'pcs';
        })
        .catch(err => console.error('Error loading garment stats:', err));
}

// Load fabric movement chart
function loadFabricMovementChart() {
    fetch('/api/fabric-movements')
        .then(res => res.json())
        .then(movements => {
            const labels = getLast7Days();
            const stockIn = new Array(7).fill(0);
            const stockOut = new Array(7).fill(0);

            const today = new Date();
            movements.forEach(m => {
                const movementDate = new Date(m.movementDate);
                const diffDays = Math.floor((today - movementDate) / (1000 * 60 * 60 * 24));

                if (diffDays >= 0 && diffDays < 7) {
                    const index = 6 - diffDays;
                    if (m.status === 'In') {
                        stockIn[index] += m.quantity;
                    } else if (m.status === 'Out') {
                        stockOut[index] += m.quantity;
                    }
                }
            });

            fabricMovementChart.data.labels = labels;
            fabricMovementChart.data.datasets[0].data = stockIn;
            fabricMovementChart.data.datasets[1].data = stockOut;
            fabricMovementChart.update();
        })
        .catch(err => console.error('Error loading fabric movements:', err));
}

// Load garment movement chart
function loadGarmentMovementChart() {
    fetch('/api/garment-movements')
        .then(res => res.json())
        .then(movements => {
            const labels = getLast7Days();
            const stockIn = new Array(7).fill(0);
            const shipped = new Array(7).fill(0);

            const today = new Date();
            movements.forEach(m => {
                const movementDate = new Date(m.movementDate);
                const diffDays = Math.floor((today - movementDate) / (1000 * 60 * 60 * 24));

                if (diffDays >= 0 && diffDays < 7) {
                    const index = 6 - diffDays;
                    if (m.status === 'In') {
                        stockIn[index] += m.quantity;
                    } else if (m.status === 'Shipped') {
                        shipped[index] += m.quantity;
                    }
                }
            });

            garmentMovementChart.data.labels = labels;
            garmentMovementChart.data.datasets[0].data = stockIn;
            garmentMovementChart.data.datasets[1].data = shipped;
            garmentMovementChart.update();
        })
        .catch(err => console.error('Error loading garment movements:', err));
}

// Load fabric low stock alerts
function loadFabricLowStockAlerts() {
    fetch('/api/dashboard/low-stock')
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById('fabricLowStockList');

            if (data && data.length > 0) {
                container.innerHTML = '';
                data.forEach(item => {
                    const isCritical = item.currentQuantity < (item.threshold * 0.5);
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
                            <p><strong>Fabric ID:</strong> ${item.fabricId}</p>
                            <p><strong>Current Stock:</strong> ${item.currentQuantity.toFixed(2)}m (${item.shortage.toFixed(2)}m below threshold of ${item.threshold}m)</p>
                            <p><strong>Reorder Level:</strong> ${item.reorderLevel}m</p>
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

// Load garment low stock alerts
function loadGarmentLowStockAlerts() {
    fetch('/api/garments/dashboard/low-stock')
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById('garmentLowStockList');

            if (data && data.length > 0) {
                container.innerHTML = '';
                data.forEach(item => {
                    const isCritical = item.criticalLevel;
                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'activity-item';
                    alertDiv.innerHTML = `
                        <div class="activity-icon ${isCritical ? 'critical' : 'warning'}">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <div class="activity-content">
                            <h5>
                                Garment ${item.garmentType} (${item.size}) ${item.fabricType} ${item.color} is below reorder level.
                                ${isCritical ? '<span class="alert-badge critical">CRITICAL</span>' : '<span class="alert-badge warning">LOW</span>'}
                            </h5>
                            <p><strong>Garment ID:</strong> ${item.garmentId}</p>
                            <p><strong>Current Stock:</strong> ${item.currentQuantity}pcs (${item.shortage}pcs below threshold of ${item.threshold}pcs)</p>
                            <p><strong>Reorder Level:</strong> ${item.reorderLevel}pcs</p>
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
    initializeCharts();
    loadDashboardData();
});

// Responsive chart resize
window.addEventListener('resize', function() {
    if (fabricMovementChart) fabricMovementChart.resize();
    if (garmentMovementChart) garmentMovementChart.resize();
});