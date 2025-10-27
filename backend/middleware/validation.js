const { body, validationResult } = require('express-validator');

const handleValidationErrors = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({
            success: false,
            message: 'Validation failed',
            errors: errors.array()
        });
    }
    next();
};

const registerValidation = [
    body('name').notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Please include a valid email'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    body('phone').optional().isMobilePhone().withMessage('Please provide a valid phone number'),
    body('role').isIn(['admin', 'client', 'driver']).withMessage('Role must be admin, client, or driver')
];

const loginValidation = [
    body('email').isEmail().withMessage('Please include a valid email'),
    body('password').exists().withMessage('Password is required')
];

const busValidation = [
    body('bus_number').notEmpty().withMessage('Bus number is required'),
    body('license_plate').notEmpty().withMessage('License plate is required'),
    body('capacity').isInt({ min: 1 }).withMessage('Capacity must be a positive number'),
    body('model').optional().notEmpty().withMessage('Model cannot be empty'),
    body('status').isIn(['active', 'maintenance', 'inactive']).withMessage('Invalid status'),
    body('driver_id').optional().isInt({ min: 1 }).withMessage('Driver ID must be a positive integer')
];

const scheduleValidation = [
    body('bus_id').isInt({ min: 1 }).withMessage('Valid bus ID is required'),
    body('route_name').notEmpty().withMessage('Route name is required'),
    body('departure_location').notEmpty().withMessage('Departure location is required'),
    body('arrival_location').notEmpty().withMessage('Arrival location is required'),
    body('departure_time').isISO8601().withMessage('Valid departure time is required'),
    body('arrival_time').isISO8601().withMessage('Valid arrival time is required'),
    body('price').isFloat({ min: 0 }).withMessage('Price must be a positive number'),
    body('available_seats').isInt({ min: 0 }).withMessage('Available seats must be a non-negative integer')
];

module.exports = {
    handleValidationErrors,
    registerValidation,
    loginValidation,
    busValidation,
    scheduleValidation
};