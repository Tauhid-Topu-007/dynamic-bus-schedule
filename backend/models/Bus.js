const db = require('../config/database');

class Bus {
    static async create(busData) {
        const { bus_number, license_plate, capacity, model, status = 'active', driver_id } = busData;

        const result = await db.query(
            'INSERT INTO buses (bus_number, license_plate, capacity, model, status, driver_id) VALUES (?, ?, ?, ?, ?, ?)',
            [bus_number, license_plate, capacity, model, status, driver_id]
        );

        return this.findById(result.insertId);
    }

    static async findById(id) {
        const buses = await db.query(`
            SELECT b.*, u.name as driver_name, u.email as driver_email
            FROM buses b
            LEFT JOIN users u ON b.driver_id = u.id
            WHERE b.id = ?
        `, [id]);
        return buses[0] || null;
    }

    static async findAll() {
        return await db.query(`
            SELECT b.*, u.name as driver_name, u.email as driver_email
            FROM buses b
            LEFT JOIN users u ON b.driver_id = u.id
            ORDER BY b.created_at DESC
        `);
    }

    static async update(id, busData) {
        const { bus_number, license_plate, capacity, model, status, driver_id } = busData;
        await db.query(
            'UPDATE buses SET bus_number = ?, license_plate = ?, capacity = ?, model = ?, status = ?, driver_id = ? WHERE id = ?',
            [bus_number, license_plate, capacity, model, status, driver_id, id]
        );
        return this.findById(id);
    }

    static async delete(id) {
        return await db.query('DELETE FROM buses WHERE id = ?', [id]);
    }

    static async findByDriverId(driverId) {
        return await db.query('SELECT * FROM buses WHERE driver_id = ?', [driverId]);
    }

    static async updateStatus(id, status) {
        await db.query('UPDATE buses SET status = ? WHERE id = ?', [status, id]);
        return this.findById(id);
    }
}

module.exports = Bus;