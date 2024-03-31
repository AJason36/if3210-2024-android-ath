import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ath.bondoman.util.isNetworkAvailable

class NetworkChangeReceiver(private val context: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!isNetworkAvailable(context)) {
            showNetworkAlertDialog()
        }
    }

    private fun showNetworkAlertDialog() {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle("No Internet Connection")
            .setMessage("Your device is currently not connected to the internet. Some features might not be available at the moment.")
            .setPositiveButton("OK", null)
            .show()
    }
}
