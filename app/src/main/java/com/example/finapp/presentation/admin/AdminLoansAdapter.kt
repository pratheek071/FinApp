package com.example.finapp.presentation.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finapp.data.model.Loan
import com.example.finapp.data.model.LoanStatus
import com.example.finapp.databinding.ItemAdminLoanBinding
import com.example.finapp.utils.DateUtils

class AdminLoansAdapter(
    private val onApprove: (Loan) -> Unit,
    private val onReject: (Loan) -> Unit,
    private val showActions: Boolean
) : ListAdapter<Loan, AdminLoansAdapter.AdminLoanViewHolder>(LoanDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminLoanViewHolder {
        val binding = ItemAdminLoanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminLoanViewHolder(binding, onApprove, onReject, showActions)
    }
    
    override fun onBindViewHolder(holder: AdminLoanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class AdminLoanViewHolder(
        private val binding: ItemAdminLoanBinding,
        private val onApprove: (Loan) -> Unit,
        private val onReject: (Loan) -> Unit,
        private val showActions: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(loan: Loan) {
            binding.apply {
                tvCustomerName.text = loan.userName
                tvPhoneNumber.text = loan.phoneNumber
                tvLoanType.text = loan.loanType.displayName
                tvAmount.text = "₹${loan.totalAmount}"
                tvDuration.text = "${loan.durationMonths} months"
                tvDailyPayment.text = "₹${loan.dailyAmount}"
                tvRequestDate.text = DateUtils.formatDate(loan.requestedAt)
                
                // Show actions for pending loans
                layoutActions.isVisible = showActions && loan.status == LoanStatus.PENDING
                if (layoutActions.isVisible) {
                    btnApprove.setOnClickListener { onApprove(loan) }
                    btnReject.setOnClickListener { onReject(loan) }
                }
                
                // Show progress for approved/active loans
                layoutProgress.isVisible = !showActions && loan.status == LoanStatus.APPROVED
                if (layoutProgress.isVisible) {
                    tvPaidAmount.text = "₹${loan.paidAmount}"
                    tvRemainingAmount.text = "₹${loan.remainingAmount}"
                }
            }
        }
    }
    
    private class LoanDiffCallback : DiffUtil.ItemCallback<Loan>() {
        override fun areItemsTheSame(oldItem: Loan, newItem: Loan): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Loan, newItem: Loan): Boolean {
            return oldItem == newItem
        }
    }
}

