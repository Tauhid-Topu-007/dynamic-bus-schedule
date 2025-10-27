const db = require('../config/database');
const bcrypt = require('bcryptjs');

class User {
    static async create(userData) {
        const { name, email, password, phone, role = 'client' } = userData;
        const hashedPassword = await bcrypt.hash(password, 10);

        const result = await db.query(
            'INSERT INTO users (name, email, password, phone, role) VALUES (?, ?, ?, ?, ?)',
            [name, email, hashedPassword, phone, role]
        );

        return this.findById(result.insertId);
    }

    static async findByEmail(email) {
        const users = await db.query('SELECT * FROM users WHERE email = ?', [email]);
        return users[0] || null;
    }

    static async findById(id) {
        const users = await db.query('SELECT id, name, email, phone, role, created_at FROM users WHERE id = ?', [id]);
        return users[0] || null;
    }

    static async findAll(role = null) {
        let query = 'SELECT id, name, email, phone, role, created_at FROM users';
        const params = [];

        if (role) {
            query += ' WHERE role = ?';
            params.push(role);
        }

        query += ' ORDER BY created_at DESC';
        return await db.query(query, params);
    }

    static async update(id, userData) {
        const { name, email, phone, role } = userData;
        await db.query(
            'UPDATE users SET name = ?, email = ?, phone = ?, role = ? WHERE id = ?',
            [name, email, phone, role, id]
        );
        return this.findById(id);
    }

    static async delete(id) {
        return await db.query('DELETE FROM users WHERE id = ?', [id]);
    }

    static async comparePassword(plainPassword, hashedPassword) {
        return await bcrypt.compare(plainPassword, hashedPassword);
    }

    static async getDrivers() {
        return await this.findAll('driver');
    }

    static async getClients() {
        return await this.findAll('client');
    }
}

module.exports = User;