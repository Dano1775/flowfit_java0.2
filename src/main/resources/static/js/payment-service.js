// payment-service.js - Servicio para comunicación con el backend de FlowFit

/**
 * Servicio que maneja la comunicación con el backend para pagos de MercadoPago
 * Integrado con la arquitectura de FlowFit
 */
class PaymentService {
    constructor() {
        this.baseUrl = window.location.origin;
        this.publicKey = null;
    }

    /**
     * Inicializa el servicio obteniendo la configuración de MercadoPago desde el backend
     * @returns {Promise<Object>} Configuración con publicKey y locale
     */
    async initialize() {
        try {
            const response = await fetch(`${this.baseUrl}/pagos/config`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Error al obtener configuración de MercadoPago');
            }

            const config = await response.json();
            this.publicKey = config.publicKey;
            
            console.log('✅ Configuración de MercadoPago obtenida:', {
                publicKey: config.publicKey.substring(0, 20) + '...',
                locale: config.locale,
                testMode: config.testMode
            });

            return {
                publicKey: config.publicKey,
                locale: config.locale || 'es-CO',
                testMode: config.testMode
            };

        } catch (error) {
            console.error('❌ Error al inicializar PaymentService:', error);
            throw error;
        }
    }

    /**
     * Procesa el pago enviando el token de MercadoPago al backend
     * @param {Object} paymentData - Datos del pago desde el Brick
     * @returns {Promise<Object>} Respuesta del backend con el resultado del pago
     */
    async processPayment(paymentData) {
        try {
            console.log('📤 Enviando pago al backend:', {
                token: paymentData.token.substring(0, 20) + '...',
                amount: paymentData.transactionAmount,
                paymentMethodId: paymentData.paymentMethodId
            });

            const response = await fetch(`${this.baseUrl}/pagos/procesar`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(paymentData)
            });

            const result = await response.json();

            if (!response.ok) {
                console.error('❌ Error en respuesta del backend:', result);
                throw new Error(result.message || result.error || 'Error al procesar el pago');
            }

            console.log('✅ Pago procesado exitosamente:', {
                id: result.id,
                status: result.status,
                amount: result.transactionAmount
            });

            return result;

        } catch (error) {
            console.error('❌ Error al procesar pago:', error);
            throw error;
        }
    }

    /**
     * Obtiene el estado de un pago específico
     * @param {string} paymentId - ID del pago en MercadoPago
     * @returns {Promise<Object>} Estado del pago
     */
    async getPaymentStatus(paymentId) {
        try {
            const response = await fetch(`${this.baseUrl}/pagos/status/${paymentId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error('Error al obtener estado del pago');
            }

            return await response.json();

        } catch (error) {
            console.error('❌ Error al obtener estado del pago:', error);
            throw error;
        }
    }

    /**
     * Verifica si el servicio está en modo TEST
     * @returns {Promise<boolean>} true si está en modo TEST
     */
    async isTestMode() {
        try {
            const response = await fetch(`${this.baseUrl}/pagos/test-mode`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                return false;
            }

            const data = await response.json();
            return data.testMode === true;

        } catch (error) {
            console.warn('⚠️ No se pudo verificar modo TEST:', error);
            return false;
        }
    }
}
