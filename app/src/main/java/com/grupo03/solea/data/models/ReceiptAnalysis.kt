package com.grupo03.solea.data.models

import com.google.gson.annotations.SerializedName

data class ReceiptAnalysis(
    val source: Source?,
    @SerializedName("store_info") val storeInfo: StoreInfo?,
    @SerializedName("issuer_name") val issuerName: String?,
    @SerializedName("vendor_name") val vendorName: String?, // Nueva estructura del servidor
    @SerializedName("document_type") val documentType: String?,
    @SerializedName("document_number") val documentNumber: String?,
    @SerializedName("transaction_details") val transactionDetails: TransactionDetails?,
    @SerializedName("cashier_info") val cashierInfo: CashierInfo?,
    val items: List<ReceiptItem>?,
    @SerializedName("line_items") val lineItems: List<ReceiptItem>?, // Nueva estructura del servidor
    val totals: Totals?,
    val summary: Summary?,
    @SerializedName("total_amount") val totalAmount: Double?, // Nuevo campo del servidor
    @SerializedName("payment_info") val paymentInfo: PaymentInfo?,
    @SerializedName("payment_details") val paymentDetails: PaymentDetails?, // Nuevo campo
    @SerializedName("quality_checks") val qualityChecks: QualityChecks?,
    @SerializedName("mapping_hints") val mappingHints: MappingHints?
) {
    // Computed properties para compatibilidad
    val computedStoreInfo: StoreInfo?
        get() = storeInfo ?: issuerName?.let { StoreInfo(name = it, ruc = null, address = null, city = null, country = null, extra = null) }
            ?: vendorName?.let { StoreInfo(name = it, ruc = null, address = null, city = null, country = null, extra = null) }
    
    val computedTotals: Totals?
        get() = totals ?: summary?.let { 
            Totals(
                subtotalPrinted = it.subtotalTaxable,
                discountsPrinted = null,
                taxPrinted = it.taxDetails?.firstOrNull()?.amount,
                serviceChargePrinted = null,
                totalPrinted = it.totalAmount
            )
        } ?: totalAmount?.let {
            Totals(
                subtotalPrinted = null,
                discountsPrinted = null,
                taxPrinted = null,
                serviceChargePrinted = null,
                totalPrinted = it
            )
        }
    
    val computedItems: List<ReceiptItem>?
        get() = items ?: lineItems
}

data class Source(
    @SerializedName("input_type") val inputType: String?,
    @SerializedName("ocr_engine") val ocrEngine: String?,
    @SerializedName("raw_text_excerpt") val rawTextExcerpt: String?
)

data class StoreInfo(
    val name: String?,
    val ruc: String?,
    val address: String?,
    val city: String?,
    val country: String?,
    val extra: Map<String, Any>?
)

data class TransactionDetails(
    @SerializedName("document_type") val documentType: String?,
    @SerializedName("document_number") val documentNumber: String?,
    @SerializedName("date_printed") val datePrinted: String?,
    @SerializedName("time_printed") val timePrinted: String?,
    @SerializedName("iso_datetime_local") val isoDatetimeLocal: String?,
    val currency: String?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("pos_terminal") val posTerminal: String?,
    @SerializedName("seq_number") val seqNumber: String?
)

data class CashierInfo(
    val name: String?
)

data class ReceiptItem(
    @SerializedName("line_index") val lineIndex: Int? = null,
    @SerializedName("product_code") val productCode: String? = null,
    val code: String? = null, // Nuevo campo para el código del servidor
    val barcode: String? = null,
    val description: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    @SerializedName("unit_price") val unitPrice: Double? = null,
    val discount: Double? = null,
    @SerializedName("discount_amount") val discountAmount: Double? = null, // Nuevo campo del servidor
    @SerializedName("total_amount") val totalAmount: Double? = null, // Nuevo campo del servidor
    @SerializedName("line_total_printed") val lineTotalPrinted: Double? = null,
    @SerializedName("line_total_computed") val lineTotalComputed: Double? = null,
    @SerializedName("line_total_mismatch") val lineTotalMismatch: Boolean? = null,
    @SerializedName("product_type") val productType: String? = null,
    @SerializedName("product_subtype") val productSubtype: String? = null,
    @SerializedName("brand_guess") val brandGuess: String? = null,
    val notes: String? = null,
    @SerializedName("line_total") val lineTotal: Double? = null // Nuevo campo del servidor
) {
    // Computed property para obtener el precio total con fallback
    val computedTotal: Double?
        get() = totalAmount ?: lineTotalPrinted ?: lineTotal
}

data class Totals(
    @SerializedName("subtotal_printed") val subtotalPrinted: Double?,
    @SerializedName("discounts_printed") val discountsPrinted: Double?,
    @SerializedName("tax_printed") val taxPrinted: Double?,
    @SerializedName("service_charge_printed") val serviceChargePrinted: Double?,
    @SerializedName("total_printed") val totalPrinted: Double?
)

data class QualityChecks(
    @SerializedName("sum_of_lines") val sumOfLines: Double?,
    @SerializedName("difference_vs_total") val differenceVsTotal: Double?,
    @SerializedName("lines_match_total") val linesMatchTotal: Boolean?
)

data class MappingHints(
    @SerializedName("movement_candidates") val movementCandidates: List<MovementCandidate>?
)

data class MovementCandidate(
    val scope: String?,
    val amount: Double?,
    val item: String?,
    val note: String?,
    @SerializedName("type_candidate") val typeCandidate: TypeCandidate?
)

data class TypeCandidate(
    val value: String?,
    val description: String?
)

// Nuevas clases para el formato del servidor
data class Summary(
    @SerializedName("subtotal_taxable") val subtotalTaxable: Double?,
    @SerializedName("subtotal_exempt") val subtotalExempt: Double?,
    @SerializedName("subtotal_unaffected") val subtotalUnaffected: Double?,
    @SerializedName("tax_details") val taxDetails: List<TaxDetail>?,
    @SerializedName("total_amount_text") val totalAmountText: String?,
    @SerializedName("total_amount") val totalAmount: Double?,
    val currency: String?
)

data class TaxDetail(
    val type: String?,
    val rate: Double?,
    val amount: Double?
)

data class PaymentInfo(
    val method: String?,
    @SerializedName("total_paid_amount") val totalPaidAmount: Double?,
    @SerializedName("rounding_amount") val roundingAmount: Double?,
    @SerializedName("change_amount") val changeAmount: Double?,
    val currency: String?
)

data class PaymentDetails(
    @SerializedName("total_paid") val totalPaid: Double?,
    @SerializedName("tendered_amount") val tenderedAmount: Double?,
    @SerializedName("change_amount") val changeAmount: Double?,
    @SerializedName("payment_methods") val paymentMethods: List<PaymentMethod>?
)

data class PaymentMethod(
    val method: String?,
    val amount: Double?
)