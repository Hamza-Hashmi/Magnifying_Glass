package com.example.magnifyingglass.magnifier.ui.dialogs


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.databinding.LayoutRattingDialogBinding
import com.example.magnifyingglass.magnifier.utils.rateUs


class RattingDialog (context: Context): Dialog(context,  R.style.ratingDialogStyle) {

    private lateinit var binding: LayoutRattingDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutRattingDialogBinding .inflate(LayoutInflater.from(context))
        setContentView(binding.root)


        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(context.getWindowWidth(0.90f), ViewGroup.LayoutParams.WRAP_CONTENT)
       setCancelable(true)

        binding.simpleRatingBar.setOnRatingBarChangeListener { _, rating, _ ->

            when {

                rating >= 5.0f -> {
                   // binding.feedbackBtn.text = "Rate Us"
                    context.rateUs()
                    dismiss()
                }
                else -> {
                    //binding.feedbackBtn.text = "Feedback"
                    //context.sendEmail()
                    dismiss()
                }
            }
        }

        binding.btnYes.setOnClickListener {

            when {
                binding.simpleRatingBar.rating == 0.0f -> {
                    Toast.makeText(context, "Kindly Rate Our Application", Toast.LENGTH_SHORT).show()
                }
                binding.simpleRatingBar.rating >= 4.0f -> {
                  context.rateUs()
                   dismiss()
                }
                else -> {
                    //context.sendEmail()
                    dismiss()
                }
            }
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }


    }
}

    fun Context.getWindowWidth(percent: Float = 1.0f): Int {
        val displayMetrics = resources.displayMetrics
        return (displayMetrics.widthPixels * percent).toInt()
    }

