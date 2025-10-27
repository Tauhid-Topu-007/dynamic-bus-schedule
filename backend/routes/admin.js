const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { auth, authorize } = require('../middleware/auth');

// All admin routes require authentication and admin role
router.use(auth);
router.use(authorize('admin'));

// Dashboard routes
router.get('/dashboard/stats', adminController.getDashboardStats);
router.get('/dashboard/activities', adminController.getRecentActivities);
router.get('/dashboard/overview', adminController.getSystemOverview);

// Report routes
router.post('/reports/generate', adminController.generateReport);

// Admin profile routes
router.get('/profile', adminController.getAdminProfile);
router.put('/profile', adminController.updateAdminProfile);

module.exports = router;