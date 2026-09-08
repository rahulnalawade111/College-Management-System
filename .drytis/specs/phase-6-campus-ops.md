# Phase 6 — Library, hostel, transport

## Goal
Campus operations modules: library with configurable fine rules, hostel blocks/rooms/beds
with allocation, transport buses/routes/stops with allocation; each with admin management
and student self-view.

## Files
- backend V7: books, library_transactions, hostels, rooms, hostel_allocations, buses,
  routes, transport_allocations (+ seed books/hostels/buses).
- Library: Book CRUD (isbn unique, total/available copies), issue/return endpoints
  (available_copies integrity, due date, fine = days_overdue × rate from settings,
  configurable), student view of issued books + fines.
- Hostel: Hostel/Room CRUD, allocation (capacity check, occupied count maintained),
  vacate (frees bed), student's own allocation + hostel fee.
- Transport: Bus/Route CRUD, allocation with stop + fare, student's own allocation + fee.
- Frontend admin pages per module (tables, forms, allocation dialogs, occupancy
  visualization) + student pages (my library / hostel / transport).

## Acceptance criteria
- [ ] Issuing a book decrements available_copies; issuing the last copy blocks further
      issues until returned.
- [ ] Returning a book 5 days after due date with the configured rate shows a fine of
      5 × rate, and the transaction history reflects it.
- [ ] Allocating a student to a full room is rejected; vacating makes the bed available again.
- [ ] Bus allocation shows driver, route, stop and monthly fare; route capacity respected.
- [ ] Students see their own library/hostel/transport records only.
- [ ] Library/hostel/transport fee components feed the student fee summary consistently
      with Phase 5.

## Tests
Fine calculation (boundary: exactly on due date = no fine, 1 day late), availability
concurrency guard, capacity checks, allocation/vacate transitions.

## Edge cases
Lost book handling (fine = book price via status), double return rejected, overlapping
hostel allocation for same student, student allocated to transport and hostel both.
