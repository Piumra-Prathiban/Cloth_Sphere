// timeDisplay.js - Display current time and date information

class TimeDisplay {
    constructor() {
        this.timeUpdateInterval = null;
        this.init();
    }

    init() {
        this.startTimeUpdates();
    }

    startTimeUpdates() {
        // Update time immediately
        this.updateTime();

        // Update time every second
        this.timeUpdateInterval = setInterval(() => {
            this.updateTime();
        }, 1000);
    }

    updateTime() {
        const now = new Date();

        // Update current time
        const timeElement = document.getElementById('currentTime');
        if (timeElement) {
            timeElement.textContent = now.toLocaleTimeString();
        }

        // Update current date
        const dateElement = document.getElementById('currentDate');
        if (dateElement) {
            dateElement.textContent = now.toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }

        // Update date information
        this.updateDateInfo(now);
    }

    updateDateInfo(date) {
        // Day of week
        const dayOfWeekElement = document.getElementById('dayOfWeek');
        if (dayOfWeekElement) {
            dayOfWeekElement.textContent = date.toLocaleDateString('en-US', { weekday: 'long' });
        }

        // Week number
        const weekNumberElement = document.getElementById('weekNumber');
        if (weekNumberElement) {
            weekNumberElement.textContent = this.getWeekNumber(date);
        }

        // Days in month
        const daysInMonthElement = document.getElementById('daysInMonth');
        if (daysInMonthElement) {
            const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);
            daysInMonthElement.textContent = lastDay.getDate();
        }
    }

    getWeekNumber(date) {
        const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
        const pastDaysOfYear = (date - firstDayOfYear) / 86400000;
        return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
    }

    // Clean up when leaving the page
    destroy() {
        if (this.timeUpdateInterval) {
            clearInterval(this.timeUpdateInterval);
        }
    }
}

// Initialize time display when DOM is loaded
let timeDisplay;
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing Time Display...');
    timeDisplay = new TimeDisplay();
});

// Clean up when leaving the page
window.addEventListener('beforeunload', function() {
    if (timeDisplay) {
        timeDisplay.destroy();
    }
});