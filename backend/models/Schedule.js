const db = require('../config/database');

class Schedule {
    static async create(scheduleData) {
        const { bus_id, route_name, departure_location, arrival_location, departure_time, arrival_time, price, available_seats } = scheduleData;

        const result = await db.query(
            `INSERT INTO schedules (bus_id, route_name, departure_location, arrival_location, departure_time, arrival_time, price, available_seats)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
            [bus_id, route_name, departure_location, arrival_location, departure_time, arrival_time, price, available_seats]
        );

        return this.findById(result.insertId);
    }

    static async findById(id) {
        const schedules = await db.query(`
            SELECT s.*, b.bus_number, b.license_plate, b.capacity, u.name as driver_name
            FROM schedules s
            JOIN buses b ON s.bus_id = b.id
            LEFT JOIN users u ON b.driver_id = u.id
            WHERE s.id = ?
        `, [id]);
        return schedules[0] || null;
    }

    static async findAll(filters = {}) {
        let query = `
            SELECT s.*, b.bus_number, b.license_plate, b.capacity, u.name as driver_name
            FROM schedules s
            JOIN buses b ON s.bus_id = b.id
            LEFT JOIN users u ON b.driver_id = u.id
            WHERE 1=1
        `;
        const params = [];

        if (filters.departure_location) {
            query += ' AND s.departure_location LIKE ?';
            params.push(`%${filters.departure_location}%`);
        }

        if (filters.arrival_location) {
            query += ' AND s.arrival_location LIKE ?';
            params.push(`%${filters.arrival_location}%`);
        }

        if (filters.departure_date) {
            query += ' AND DATE(s.departure_time) = ?';
            params.push(filters.departure_date);
        }

        if (filters.status) {
            query += ' AND s.status = ?';
            params.push(filters.status);
        }

        query += ' ORDER BY s.departure_time ASC';
        return await db.query(query, params);
    }

    static async update(id, scheduleData) {
        const { bus_id, route_name, departure_location, arrival_location, departure_time, arrival_time, price, available_seats, status } = scheduleData;

        await db.query(
            `UPDATE schedules SET bus_id = ?, route_name = ?, departure_location = ?, arrival_location = ?,
             departure_time = ?, arrival_time = ?, price = ?, available_seats = ?, status = ? WHERE id = ?`,
            [bus_id, route_name, departure_location, arrival_location, departure_time, arrival_time, price, available_seats, status, id]
        );

        return this.findById(id);
    }

    static async delete(id) {
        return await db.query('DELETE FROM schedules WHERE id = ?', [id]);
    }

    static async updateAvailableSeats(id, seats) {
        await db.query('UPDATE schedules SET available_seats = ? WHERE id = ?', [seats, id]);
        return this.findById(id);
    }

    static async updateStatus(id, status) {
        await db.query('UPDATE schedules SET status = ? WHERE id = ?', [status, id]);
        return this.findById(id);
    }

    static async findByBusId(busId) {
        return await db.query(`
            SELECT s.*, b.bus_number, b.license_plate
            FROM schedules s
            JOIN buses b ON s.bus_id = b.id
            WHERE s.bus_id = ?
            ORDER BY s.departure_time ASC
        `, [busId]);
    }
}

module.exports = Schedule;