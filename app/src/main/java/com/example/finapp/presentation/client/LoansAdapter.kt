package com.example.finapp.presentation.client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finapp.R
import com.example.finapp.data.model.Loan
import com.example.finapp.data.model.LoanStatus
import com.example.finapp.databinding.ItemLoanBinding
import com.example.finapp.utils.DateUtils

class LoansAdapter(
    private val onPayNowClick: (Loan) -> Unit
) : ListAdapter<Loan, LoansAdapter.LoanViewHolder>(LoanDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val binding = ItemLoanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoanViewHolder(binding, onPayNowClick)
    }
    
    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class LoanViewHolder(
        private val binding: ItemLoanBinding,
        private val onPayNowClick: (Loan) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(loan: Loan) {
            binding.apply {
                tvLoanType.text = loan.loanType.displayName
                tvAmount.text = "Total Amount: ₹${loan.totalAmount}"
                tvDailyPayment.text = "Daily Payment: ₹${loan.dailyAmount}"
                tvDate.text = "Requested: ${DateUtils.formatDate(loan.requestedAt)}"
                
                // Set status
                tvStatus.text = loan.status.name
                val statusColor = when (loan.status) {
                    LoanStatus.PENDING -> R.color.warning
                    LoanStatus.APPROVED -> R.color.success
                    LoanStatus.REJECTED -> R.color.error
                    LoanStatus.COMPLETED -> R.color.gray
                }
                tvStatus.setTextColor(
                    ContextCompat.getColor(itemView.context, statusColor)
                )
                
                // Show progress for approved loans
                layoutProgress.isVisible = loan.status == LoanStatus.APPROVED
                if (loan.status == LoanStatus.APPROVED) {
                    tvPaidAmount.text = "₹${loan.paidAmount}"
                    tvRemainingAmount.text = "₹${loan.remainingAmount}"
                    
                    btnPayNow.setOnClickListener {
                        onPayNowClick(loan)
                    }
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

