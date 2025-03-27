package com.dias.itemcheckdemo

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.densowave.scannersdk.Common.CommException
import com.densowave.scannersdk.Common.CommManager
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Dto.RFIDScannerFilter
import com.densowave.scannersdk.Dto.RFIDScannerSettings
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.Listener.ScannerAcceptStatusListener
import com.densowave.scannersdk.RFID.RFIDData
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.densowave.scannersdk.RFID.RFIDException
import com.densowave.scannersdk.RFID.RFIDScanner
import com.dias.itemcheckdemo.databinding.ActivityMainBinding
import java.io.File


class MainActivity : BaseActivity(), ScannerAcceptStatusListener, RFIDDataDelegate {
    lateinit var binding: ActivityMainBinding

    private  val  TAG = "DEMO_SP1"
    // The key value to exchange data with Service
    val serviceKey = "serviceParam"

    private var rfidScanner: RFIDScanner? = null
    private var orgScannerSettings: RFIDScannerSettings? = null
    val LIGHT_ON = 1
    val LIGHT_OFF = 0


    // Declaring the DataModel Array
    private var dataModel: ArrayList<DataModel>? = null

    // Declaring the elements from the main layout file
    private lateinit var lstAdapter: CustomAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Set the TOP-Activity
        setTopActivity(true)

        setContentView(binding.root)

        binding.imgSetting.visibility = View.INVISIBLE
        binding.imgRefresh.visibility = View.INVISIBLE
        binding.tilAutoComplete.isEnabled = false

        // Service is started in the back ground.
        super.startService()


        binding.imgSetting.bringToFront()
        binding.imgSetting.setOnClickListener {
            var intent = Intent(application,SettingActivity::class.java)
            startActivity(intent)
        }

        binding.imgSp1Connect.bringToFront()
        binding.imgSp1Connect.setOnClickListener {
            // Restart SP1 connection.
            connectToSp1()
        }

        val data = arrayOf("Clothing","Box")
        val adapter = ArrayAdapter(this,R.layout.drop_down_item,data)

        val autoCompleteTxt = findViewById<AutoCompleteTextView>(R.id.filled_exposed)
        autoCompleteTxt.setAdapter(adapter)

        autoCompleteTxt.setOnItemClickListener { adapterView, view, i, l ->
          //  Toast.makeText(this, autoCompleteTxt.text.toString(),Toast.LENGTH_LONG).show()
            lstAdapter.filter.filter(if(autoCompleteTxt.text.toString().lowercase().contains("single")) "1" else "2")

            if(lstAdapter.count > 0){
                binding.imgStart.visibility = View.VISIBLE
                binding.imgRefresh.visibility = View.VISIBLE
            }
        }


        // Initializing the elements from the main layout file

        dataModel = readCsv()

        // Setting the adapter
        lstAdapter = CustomAdapter(dataModel!!, this)
        binding.lstItems.adapter = lstAdapter

        // Upon item click, checkbox will be set to checked
//        binding.lstItems.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
//            val dataModel: DataModel = dataModel!![position] as DataModel
//            dataModel.checked = !dataModel.checked
//            lstAdapter.notifyDataSetChanged()
//        }

        binding.imgStart.setOnClickListener{
//            binding.imgSetting.visibility = View.INVISIBLE
//            binding.imgRefresh.visibility = View.INVISIBLE
//            binding.tilAutoComplete.isEnabled = false

            Thread(Runnable {
                runOnUiThread{
                    binding.imgSetting.visibility = View.INVISIBLE
                    binding.imgRefresh.visibility = View.INVISIBLE
                    binding.tilAutoComplete.isEnabled = false
                }

                startReadingTags()
            }).start()

//            runOnUiThread {
//                startReadingTags()
//            }

          //  startUhfTagRead()
        }

        binding.imgStop.setOnClickListener {
            rfidScanner?.close()
            binding.tilAutoComplete.isEnabled = true
            binding.imgSetting.visibility = View.VISIBLE
            binding.imgRefresh.visibility = View.VISIBLE

           // stopReadingTags()
            if(lstAdapter.count > 0){
                binding.imgStart.visibility = View.VISIBLE
                binding.imgStop.visibility = View.INVISIBLE
            }
        }

        binding.imgRefresh.setOnClickListener {

            Thread(Runnable {
                runOnUiThread{
                    binding.imgSetting.visibility = View.INVISIBLE
                    binding.imgStart.visibility = View.INVISIBLE
                    binding.imgStop.visibility = View.INVISIBLE
                    binding.tilAutoComplete.isEnabled = false
                }

                stopReadingTags()
            }).start()
        }
    }

    private fun stopReadingTags(){

        val myScannerSettings: RFIDScannerSettings = rfidScanner!!.settings
        myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.CONTINUOUS2
        myScannerSettings.scan.powerLevelRead = 30
        myScannerSettings.scan.powerLevelWrite = 30

        rfidScanner?.settings = myScannerSettings

        for (i in 0 until lstAdapter.count) {
            val data = lstAdapter.getItem(i)
            if(data.nTag){
                // Lighting process
                val result = writeTagWithLightOnOff(data.uii, LIGHT_OFF)
                if(result == 1){
                    data.checked = false
                    data.imgOn = false
                }
            }else{
                data.checked = false
                data.imgOn = false
            }

            runOnUiThread {
                lstAdapter.notifyDataSetChanged()
            }
        }

        runOnUiThread {
            if(lstAdapter.count > 0){
                binding.imgSetting.visibility = View.VISIBLE
                binding.imgStart.visibility = View.VISIBLE
                binding.imgStop.visibility = View.INVISIBLE
                binding.tilAutoComplete.isEnabled = true
            }
        }
    }

    private fun startReadingTags(){

        runOnUiThread {
            if(lstAdapter.count > 0){
                binding.imgStop.visibility = View.VISIBLE
                binding.imgStart.visibility = View.INVISIBLE
            }
        }

        val myScannerSettings: RFIDScannerSettings = rfidScanner!!.settings
        myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.CONTINUOUS2
        myScannerSettings.scan.powerLevelRead = 30
        myScannerSettings.scan.powerLevelWrite = 30

        rfidScanner?.settings = myScannerSettings


        // 1st Loop for NaviTag.
        for (i in 0 until lstAdapter.count) {
            val data = lstAdapter.getItem(i)

            if(data.nTag){
                if(binding.imgStop.visibility == View.INVISIBLE)        // User stop the reading.
                    return

                // Light On all NaviTag first.
                val result = writeTagWithLightOnOff(data.uii, LIGHT_ON)
                if(result == 1){
                    data.checked = true
                    data.imgOn = true
                }
                runOnUiThread {
                    lstAdapter.notifyDataSetChanged()
                }

            }
        }

        startUhfTagRead()
    }

    private fun startUhfTagRead(){
        // Once NaviTag finished. Now it is for normal UHF tags.
        // Open Inventory for normal UHF tags.

        val myScannerSettings: RFIDScannerSettings = rfidScanner!!.settings
        myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.CONTINUOUS1
        myScannerSettings.scan.powerLevelRead = 30
        myScannerSettings.scan.powerLevelWrite = 30
       // myScannerSettings.scan.doubleReading = DoubleReading.PREVENT1

        rfidScanner?.settings = myScannerSettings


        val filter = RFIDScannerFilter()
        filter.bank = RFIDScannerFilter.Bank.UII
        filter.bitOffset = 32 // bit offset to skip CRC(16bits) and PC(16bits)

        filter.filterData = byteArrayOf(
            0x03,
            0x03,
            0x52
        ) // filter data as byte array ("30352" -> 0x030352 -> 0x03,0x03,0x52)

        filter.bitLength = 20 // used length of filter data (in this case, use lower 20 bits 0x30352 from 0x030352)

        val filterArr = arrayOf(filter)
        rfidScanner!!.setFilter(filterArr, RFIDScannerFilter.RFIDLogicalOpe.OR)

        rfidScanner?.openInventory()

        runOnUiThread {
            if(lstAdapter.count > 0){
                binding.imgStop.visibility = View.VISIBLE
                binding.imgStart.visibility = View.INVISIBLE
            }
        }

    }


    // Turns on / off the LED of the specified TAG
    private fun writeTagWithLightOnOff(serialNo: String, onOff: Int): Int {

        return try {
            if (onOff == LIGHT_ON) {
                rfidScanner?.navigationTag?.lightOn(stringToByte(serialNo))
                rfidScanner?.navigationTag?.stopLightOnByTrigger()
            } else {
                rfidScanner?.navigationTag?.lightOff(stringToByte(serialNo))
            }
            1
        } catch (e: RFIDException) {
            e.printStackTrace()
            Log.d("LightTag", "Error22")
            0
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                CommManager.endAccept()
                CommManager.removeAcceptStatusListener(this@MainActivity)
                finish()
                return true
            }
        }
        return false
    }



    private fun readCsv(): ArrayList<DataModel> {

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "data.csv")
        var files = File(getExternalFilesDir(null),"data.csv")
       // Log.d("TMH", file.canRead().toString())
        val lstModel =ArrayList<DataModel>()


        files.forEachLine {
            line -> val (type,nTag, uii, name) = line.split(',', ignoreCase = false, limit = 6)
                    if(!line.uppercase().contains("UII"))
                    {
                        lstModel.add(
                            DataModel(
                                type.trim(),
                                nTag == "1",
                                uii.trim(),
                                name.trim(),
                                checked = false,
                                imgOn = false
                            ))
                    }

        }
        return lstModel
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume ")
        connectToSp1()
    }

    private fun connectToSp1(){
        if (!super.isCommScanner()) {
            CommManager.addAcceptStatusListener(this)
            CommManager.startAccept()
            // Draw connection status if not connected
            binding.tvStatus.text = getString(R.string.waiting_for_connection)
            binding.imgSp1Connect.setImageResource(R.drawable.sp1_off)
        } else {
            if (commScanner != null) {
                binding.imgSp1Connect.setImageResource(R.drawable.sp1_on)
                binding.tvStatus.text = commScanner!!.btLocalName
            } else {
                binding.tvStatus.text = ""
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause ")
        if(rfidScanner!=null)
            rfidScanner?.close()
        // Abort the connection request

        // Abort the connection request
        CommManager.endAccept()
        CommManager.removeAcceptStatusListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        if (super.isCommScanner()) {
            super.disconnectCommScanner()
        }
        // Abort the connection request
        // Abort the connection request
        CommManager.endAccept()

        super.onDestroy()
    }

    override fun OnScannerAppeared(mCommScanner: CommScanner) {
        var successFlag = false
        try {
            mCommScanner.claim()
            // Abort the connection request
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            successFlag = true
        } catch (e: CommException) {
            e.printStackTrace()
        }


        try {
            super.setConnectedCommScanner(mCommScanner)
            // Display BTLocalName of the connected scanner in the screen
            // Run on UI Thread using runOnUIThread
            val btLocalName: String = commScanner!!.btLocalName
            runOnUiThread {
                if (successFlag) {
                    binding.tvStatus.text = btLocalName
                    binding.imgSp1Connect.setImageResource(R.drawable.sp1_on)

                    binding.imgSetting.visibility = View.VISIBLE
                    binding.imgRefresh.visibility = View.INVISIBLE
                    binding.tilAutoComplete.isEnabled = true

                } else {
                    binding.tvStatus.text = getString(R.string.connection_error)
                    binding.imgSp1Connect.setImageResource(R.drawable.sp1_off)
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            // Get RFID scanner.
            rfidScanner = commScanner?.rfidScanner
            rfidScanner?.setDataDelegate(this)

            orgScannerSettings = rfidScanner?.settings
            val myScannerSettings: RFIDScannerSettings = rfidScanner!!.settings
            myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.CONTINUOUS2
            myScannerSettings.scan.powerLevelRead = 30
            myScannerSettings.scan.powerLevelWrite = 30

            rfidScanner?.settings = myScannerSettings
           // val sUII = "3C00000012106F4E1DD60000"
         //   rfidScanner?.navigationTag?.startLightOnByTrigger(stringToByte(sUII))
        } catch (e: RFIDException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Get scanner fail!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


           /* Temporary block below code.

            // (STR) ADD CODE SHOW TOAST VERSION SP1 20181129
            if (myScanner!!.version.contains("Ver. ")) {
                val verSP1: String = myScanner!!.version.toString().replace("Ver. ", "")
                val thread = Thread {
                    val text =
                        java.lang.String.format(getString(R.string.E_MSG_VERSION_SP1), verSP1)
                    if ("1.02".compareTo(verSP1) >= 0) {
                        runOnUiThread { super@MainActivity.showMessage(text) }
                    }
                }
                thread.start()
            }
            // (END) ADD CODE SHOW TOAST VERSION SP1 20181129


            */

    }

    private val handler: Handler = Handler()

    override fun onRFIDDataReceived(commScanner: CommScanner, rfidDataReceivedEvent: RFIDDataReceivedEvent) {
        handler.post {
            val rfidDataList: List<RFIDData> = rfidDataReceivedEvent.rfidData

            if (rfidDataList.isNotEmpty()) {
                val rfidData = rfidDataList[0]

                Log.d(TAG, "TagData: " +  byteToString(rfidData.data));
                Log.d(TAG, "UIIData: " + byteToString(rfidData.uii));

                for (i in 0 until lstAdapter.count) {
                    val data = lstAdapter.getItem(i)

                    if(!data.nTag){
                        // Light On all NaviTag first.
                        if(byteToString(rfidData.uii) == data.uii)
                        {
                            data.checked = true
                            //  data.imgOn = true
                            lstAdapter.notifyDataSetChanged()
                        }
//                        runOnUiThread{
//                            lstAdapter.notifyDataSetChanged()
//                        }
                    }
                }
            }
        }

    }

    private fun byteToString(bytes: ByteArray): String? {
        val stringBuilder = StringBuilder()
        for (b in bytes) {
            stringBuilder.append(String.format("%02X", b))
        }
        return stringBuilder.toString()
    }

    private fun stringToByte(hex: String): ByteArray? {
        val bytes = ByteArray(hex.length / 2)
        for (index in bytes.indices) {
            bytes[index] = hex.substring(index * 2, (index + 1) * 2).toInt(16).toByte()
        }
        return bytes
    }

    companion object {
        @kotlin.jvm.JvmField
        var serviceKey: String = "serviceParam"
    }

}