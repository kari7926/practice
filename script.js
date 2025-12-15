const monthLabel = document.getElementById('monthLabel');
const calendarEl = document.getElementById('calendar');
const bookingForm = document.getElementById('bookingForm');
const meetingNameInput = document.getElementById('meetingName');
const reservedByInput = document.getElementById('reservedBy');
const roomInput = document.getElementById('room');
const dateInput = document.getElementById('date');
const latestBookings = document.getElementById('latestBookings');
const bookingItemTemplate = document.getElementById('bookingItemTemplate');
const offsetWeek = document.getElementById('offsetWeek');
const offsetMonth = document.getElementById('offsetMonth');
const quickSelectButtons = document.querySelectorAll('.quick-selects button');
const clearBookings = document.getElementById('clearBookings');
const nextAvailable = document.getElementById('nextAvailable');
const prevMonth = document.getElementById('prevMonth');
const nextMonth = document.getElementById('nextMonth');
const newBookingBtn = document.getElementById('newBookingBtn');
const roomShortcuts = document.querySelectorAll('.room-shortcut');

let currentDate = new Date();
let viewDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
let bookings = [];

const formatter = new Intl.DateTimeFormat('en', {
  month: 'long',
  year: 'numeric',
});

const longDateFormatter = new Intl.DateTimeFormat('en', {
  weekday: 'short',
  month: 'short',
  day: 'numeric',
  year: 'numeric',
});

function formatInputDate(date) {
  return date.toISOString().split('T')[0];
}

function setDateInput(date) {
  dateInput.value = formatInputDate(date);
}

function getDaysInMonth(year, month) {
  return new Date(year, month + 1, 0).getDate();
}

function renderCalendar() {
  calendarEl.innerHTML = '';
  monthLabel.textContent = formatter.format(viewDate);

  const firstDay = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1).getDay();
  const daysInMonth = getDaysInMonth(viewDate.getFullYear(), viewDate.getMonth());
  const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;

  for (let cell = 0; cell < totalCells; cell += 1) {
    const dayNumber = cell - firstDay + 1;
    const dayEl = document.createElement('div');
    dayEl.className = 'day';

    if (dayNumber < 1 || dayNumber > daysInMonth) {
      dayEl.innerHTML = '<span class="label muted">&nbsp;</span>';
      calendarEl.appendChild(dayEl);
      continue;
    }

    const thisDate = new Date(viewDate.getFullYear(), viewDate.getMonth(), dayNumber);
    const isToday = formatInputDate(thisDate) === formatInputDate(currentDate);
    const label = document.createElement('span');
    label.className = 'label' + (isToday ? ' today' : '');
    label.textContent = dayNumber;
    dayEl.appendChild(label);

    const dots = document.createElement('div');
    dots.className = 'dots';
    const dayBookings = bookings.filter((b) => b.date === formatInputDate(thisDate));
    dayBookings.slice(0, 3).forEach(() => {
      dots.appendChild(document.createElement('span')).className = 'dot';
    });

    if (isToday) {
      dayEl.classList.add('today');
    }

    if (dayBookings.length) {
      dayEl.appendChild(dots);
      dayEl.title = `${dayBookings.length} booking${dayBookings.length > 1 ? 's' : ''}`;
    }

    calendarEl.appendChild(dayEl);
  }
}

function updateLatestBookings() {
  latestBookings.innerHTML = '';

  bookings
    .slice()
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .forEach((booking) => {
      const clone = bookingItemTemplate.content.cloneNode(true);
      clone.querySelector('.booking-title').textContent = booking.meeting;
      clone.querySelector('.booking-meta').textContent = `${booking.room} • ${longDateFormatter.format(
        new Date(booking.date)
      )} • ${booking.reservedBy}`;
      clone.querySelector('.badge').textContent = 'Scheduled';
      latestBookings.appendChild(clone);
    });

  const nextDate = bookings
    .map((b) => new Date(b.date))
    .filter((d) => d >= currentDate)
    .sort((a, b) => a - b)[0];

  nextAvailable.textContent = nextDate ? longDateFormatter.format(nextDate) : 'Pick a date';
}

function applyOffset(days) {
  const base = new Date();
  base.setDate(base.getDate() + days);
  setDateInput(base);
}

bookingForm.addEventListener('submit', (event) => {
  event.preventDefault();
  const meeting = meetingNameInput.value.trim();
  const reservedBy = reservedByInput.value.trim();
  const room = roomInput.value;
  const date = dateInput.value;

  if (!meeting || !reservedBy || !room || !date) return;

  bookings.push({ meeting, reservedBy, room, date });
  meetingNameInput.value = '';
  reservedByInput.value = '';
  roomInput.value = '';
  setDateInput(new Date());

  updateLatestBookings();
  renderCalendar();
});

offsetWeek.addEventListener('click', () => applyOffset(7));
offsetMonth.addEventListener('click', () => applyOffset(30));
quickSelectButtons.forEach((btn) => {
  btn.addEventListener('click', () => applyOffset(Number(btn.dataset.offset)));
});

clearBookings.addEventListener('click', () => {
  bookings = [];
  updateLatestBookings();
  renderCalendar();
});

prevMonth.addEventListener('click', () => {
  viewDate.setMonth(viewDate.getMonth() - 1);
  renderCalendar();
});

nextMonth.addEventListener('click', () => {
  viewDate.setMonth(viewDate.getMonth() + 1);
  renderCalendar();
});

newBookingBtn.addEventListener('click', () => {
  meetingNameInput.focus();
});

roomShortcuts.forEach((shortcut) => {
  shortcut.addEventListener('click', () => {
    const room = shortcut.dataset.room;
    roomInput.value = room;
    meetingNameInput.focus();
    bookingForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
});

function init() {
  setDateInput(currentDate);
  renderCalendar();
  updateLatestBookings();
}

init();
