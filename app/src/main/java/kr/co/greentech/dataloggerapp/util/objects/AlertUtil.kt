package kr.co.greentech.dataloggerapp.util.objects

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import kr.co.greentech.dataloggerapp.R

object AlertUtil {
    fun alert(
        context: Context,
        msg: String,
        okOnClickListener: DialogInterface.OnClickListener? = null,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
        builder.setNegativeButton(context.getString(R.string.ok), okOnClickListener)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    fun alertNotCancelable(
            context: Context,
            msg: String,
            okOnClickListener: DialogInterface.OnClickListener? = null,
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
        builder.setNegativeButton(context.getString(R.string.ok), okOnClickListener)
        builder.setCancelable(false)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    fun alertOkAndCancel(
            context: Context,
            titleMsg: String,
            okString: String ,
            cancelString: String = context.getString(R.string.cancel),
            cancelOnClickListener: DialogInterface.OnClickListener? = null,
            okOnClickListener: DialogInterface.OnClickListener
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(titleMsg)
        builder.setNegativeButton(cancelString, cancelOnClickListener)
        builder.setPositiveButton(okString, okOnClickListener)
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    fun alertOkAndCancelCancelable(
            context: Context,
            titleMsg: String,
            okString: String ,
            cancelString: String = context.getString(R.string.cancel),
            cancelOnClickListener: DialogInterface.OnClickListener? = null,
            okOnClickListener: DialogInterface.OnClickListener
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(titleMsg)
        builder.setNegativeButton(cancelString, cancelOnClickListener)
        builder.setPositiveButton(okString, okOnClickListener)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}