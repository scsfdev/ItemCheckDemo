package com.dias.itemcheckdemo

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import com.densowave.scannersdk.Barcode.BarcodeException
import com.densowave.scannersdk.Common.CommException
import com.densowave.scannersdk.Const.CommConst.CommBattery
import com.densowave.scannersdk.Dto.BarcodeScannerSettings
import com.densowave.scannersdk.Dto.CommScannerBtSettings
import com.densowave.scannersdk.Dto.CommScannerParams
import com.densowave.scannersdk.Dto.RFIDScannerSettings
import com.densowave.scannersdk.RFID.RFIDException
import com.dias.itemcheckdemo.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding
    private var disposeFlg = true

    private var commParams: CommScannerParams? = null
    private var commBTSetting: CommScannerBtSettings? = null
    private var rfidSettings: RFIDScannerSettings? = null
    private var barcodeSettings: BarcodeScannerSettings? = null

    private val checkboxTrue = 0
    private val checkboxTrueID = 0

    // Buzzer volume
  //  var buzzerVolumeMap: MutableMap<String, BuzzerVolume> = HashMap(3)

    // Trigger mode
  //  var triggerModeMap: MutableMap<String, BarcodeScannerSettings.Scan.TriggerMode> = HashMap(5)
    // Set the polarization

    // Set the polarization
  //  var polarizationMap: MutableMap<String, Polarization> = HashMap(3)

    //Set the reconnect
   // var reConnectMap: MutableMap<String, ReConnect> = HashMap(3)

    //Set the mode
   // var modeMap: MutableMap<String, CommScannerBtSettings.Mode> = HashMap(3)

    //Set the trying time
  //  var tryingTimeMap: MutableMap<String, TryingTime> = HashMap(2)

    //Set the waiting time
  //  var waitingTimeMap: MutableMap<String, WaitingTime> = HashMap(5)

    // RFID TRIGGER MODE

    // RFID TRIGGER MODE
    var rfid_triggerModeMap: MutableMap<String, RFIDScannerSettings.Scan.TriggerMode> = HashMap(5)


    // Whether it is connected to the scanner during generating time

    // Even when the connection is lost while on this screen, if it was connected to scanner during generating time, display the communication error

    // Whether it is connected to the scanner during generating time
    // Even when the connection is lost while on this screen, if it was connected to scanner during generating time, display the communication error
    private var scannerConnectedOnCreate = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)

        setContentView(binding.root)

        scannerConnectedOnCreate = super.isCommScanner()

        var result = true
       // var imgSp1Connect = findViewById<ImageView>(R.id.imgSp1Status)

        result = if (scannerConnectedOnCreate) {
            // Read SP1 information
            binding.imgSp1Status.setImageResource(R.drawable.sp1_on)
            loadScannerInfo()
        } else {
            // When SP1 is not found, display the error message.
            super.showMessage(getString(R.string.E_MSG_NO_CONNECTION))
            binding.imgSp1Status.setImageResource(R.drawable.sp1_off)
            false // Disconnected
        }

        binding.btnUpdateSetting.setOnClickListener {
            save()
            disposeFlg = false
            finish()
        }

        // Read the configuration value


        // Read the configuration value
        if (result) {
            loadSettings()
        }

        // Service is started in the back ground.


        // Service is started in the back ground.
        super.startService()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                // Transmit and maintain SP1 configuration value
                save()
                disposeFlg = false

                // Stop the Activity because it becomes unnecessary since the parent Activity is returned to.
                finish()
                return true
            }
        }
        return false
    }

    override fun onRestart() {
        disposeFlg = true
        super.onRestart()
    }

    override fun onUserLeaveHint() {
        if (disposeFlg) {
            // Transmit and maintain SP1 configuration value
            save()
            disposeFlg = false
        }
    }

    override fun onDestroy() {
        if (scannerConnectedOnCreate && disposeFlg) {
            super.disconnectCommScanner()
        }

        super.onDestroy()
    }

    private fun loadScannerInfo(): Boolean {
        // Acquire SP1 version
        try {
            val ver = super.getCommScanner()!!.version
//            val textView = findViewById(R.id.text_scanner_version_value) as TextView
//            textView.text = ver.replace("Ver. ", "") // Remove the character string "Ver. “

            // Process if version is 1.10
            val verF = ver.replace("Ver. ", "")
            if ("1.10".compareTo(verF) >= 0) {
                Log.d("TMH", "testing")
//                findViewById(R.id.bluetooth_layout_1).setVisibility(View.GONE)
//                findViewById(R.id.bluetooth_layout_2).setVisibility(View.GONE)
//                findViewById(R.id.bluetooth_layout_3).setVisibility(View.GONE)
//                findViewById(R.id.bluetooth_layout_4).setVisibility(View.GONE)
//                findViewById(R.id.text_blutooth_head).setVisibility(View.GONE)
            }
        } catch (e: Exception) {
            return false
        }

        // Read the remaining SP1 battery
        var battery: CommBattery? = null
        try {
            battery = super.getCommScanner()!!.remainingBattery
        } catch (e: CommException) {
            e.printStackTrace()
        }

        if (battery != null) {
            var batteryResId = 0
            batteryResId = when (battery) {
                CommBattery.UNDER10 -> R.mipmap.battery_1
                CommBattery.UNDER40 -> R.mipmap.battery_2
                CommBattery.OVER40 -> {
                    R.mipmap.battery_full
                }
            }

            val battPowerStr = when(battery){
                CommBattery.UNDER10  -> "< 10 %"
                CommBattery.UNDER40 -> "< 40 %"
                CommBattery.OVER40 -> "> 40 %"
            }

            binding.tvSp1BattPower.text = battPowerStr
            binding.imgScannerBatt.setImageResource(batteryResId)
        } else {
            // showMessage(“Failed to acquire remaining battery");
            return false
        }
        return true
    }


    private fun setMap() {

        /*
        // Buzzer volume Map
//        buzzerVolumeMap[resources.getString(R.string.buzzer_volume_low)] = BuzzerVolume.LOW
//        buzzerVolumeMap[resources.getString(R.string.buzzer_volume_middle)] = BuzzerVolume.MIDDLE
//        buzzerVolumeMap[resources.getString(R.string.buzzer_volume_loud)] = BuzzerVolume.LOUD
//
//        // Bar code trigger mode Map
//        triggerModeMap[resources.getString(R.string.trigger_mode_auto_off)] = BarcodeScannerSettings.Scan.TriggerMode.AUTO_OFF
//        triggerModeMap[resources.getString(R.string.trigger_mode_momentary)] =
//            BarcodeScannerSettings.Scan.TriggerMode.MOMENTARY
//        triggerModeMap.put(
//            resources.getString(R.string.trigger_mode_alternate),
//            BarcodeScannerSettings.Scan.TriggerMode.ALTERNATE
//        )
//        triggerModeMap.put(
//            resources.getString(R.string.trigger_mode_continuous),
//            BarcodeScannerSettings.Scan.TriggerMode.CONTINUOUS
//        )
//        triggerModeMap.put(
//            resources.getString(R.string.trigger_mode_trigger_release),
//            BarcodeScannerSettings.Scan.TriggerMode.TRIGGER_RELEASE
//        )
         */


        // RFID trigger mode Map
        rfid_triggerModeMap.put(
            resources.getString(R.string.trigger_mode_auto_off),
            RFIDScannerSettings.Scan.TriggerMode.AUTO_OFF
        )
        rfid_triggerModeMap.put(
            resources.getString(R.string.trigger_mode_momentary),
            RFIDScannerSettings.Scan.TriggerMode.MOMENTARY
        )
        rfid_triggerModeMap.put(
            resources.getString(R.string.trigger_mode_alternate),
            RFIDScannerSettings.Scan.TriggerMode.ALTERNATE
        )
        rfid_triggerModeMap.put(
            resources.getString(R.string.rfid_trigger_mode_continuous1),
            RFIDScannerSettings.Scan.TriggerMode.CONTINUOUS1
        )
        rfid_triggerModeMap.put(
            resources.getString(R.string.rfid_trigger_mode_continuous2),
            RFIDScannerSettings.Scan.TriggerMode.CONTINUOUS2
        )

        /*
        // Polarized wave setting Map
//        polarizationMap.put(resources.getString(R.string.polarization_vertical), Polarization.V)
//        polarizationMap.put(resources.getString(R.string.polarization_horizontal), Polarization.H)
//        polarizationMap.put(resources.getString(R.string.polarization_both), Polarization.Both)
//
//        // Reconnect map
//        reConnectMap.put(resources.getString(R.string.reconnect_repairing), ReConnect.REPAIRING)
//        reConnectMap.put(resources.getString(R.string.reconnect_waithost), ReConnect.WAITHOST)
//        reConnectMap.put(resources.getString(R.string.reconnect_any), ReConnect.ANY)
//
//        // Mode map
//        modeMap.put(resources.getString(R.string.mode_master), CommScannerBtSettings.Mode.MASTER)
//        modeMap.put(resources.getString(R.string.mode_slave), CommScannerBtSettings.Mode.SLAVE)
//        modeMap.put(resources.getString(R.string.mode_auto), CommScannerBtSettings.Mode.AUTO)
//
//        // Trying time map
//        tryingTimeMap.put(resources.getString(R.string.trying_time_30sec), TryingTime.T30SEC)
//        tryingTimeMap.put(resources.getString(R.string.trying_time_none), TryingTime.NONE)
//
//        // Waiting time map
//        waitingTimeMap.put(resources.getString(R.string.waiting_time_2min), WaitingTime.T2MIN)
//        waitingTimeMap.put(resources.getString(R.string.waiting_time_4min), WaitingTime.T4MIN)
//        waitingTimeMap.put(resources.getString(R.string.waiting_time_10min), WaitingTime.T10MIN)
//        waitingTimeMap.put(resources.getString(R.string.waiting_time_30min), WaitingTime.T30MIN)
//        waitingTimeMap.put(resources.getString(R.string.waiting_time_none), WaitingTime.NONE)
         */

    }


    private fun loadSettings() {

        // Create the Map
        this.setMap()
        try {
           // setChannel()
            // Acquire the configuration value related to RFID
            rfidSettings = super.getCommScanner()!!.rfidScanner.settings

            // Get Read power.
            var readPower = rfidSettings?.scan?.powerLevelRead!!

            var readPowerStr = 0f

            readPowerStr = when (readPower) {
                in 26..30 -> 30f
                in 21..25 -> 25f
                in 16..20 -> 20f
                in 11..15 -> 15f
                in 6..10 -> 10f
                else -> 5f
            }

           // val sp1PowerLevel = findViewById<Slider>(R.id.sp1PowerLevel)
            binding.sp1PowerLevel.value = readPowerStr



            // Acquire the configuration value of the common parameters
            commParams = super.getCommScanner()!!.params
            commBTSetting = super.getCommScanner()!!.btSettings

            // Acquire the configuration value related to the bar code
            barcodeSettings = super.getCommScanner()!!.barcodeScanner.settings
        } catch (e: CommException) {
            rfidSettings = null
            commParams = null
            barcodeSettings = null
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
            return
        } catch (e: RFIDException) {
            rfidSettings = null
            commParams = null
            barcodeSettings = null
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
            return
        } catch (e: BarcodeException) {
            rfidSettings = null
            commParams = null
            barcodeSettings = null
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
            return
        }

        /*


// [Configuration value related to RFID]

        // read_power_level
//        loadIntegerSetting(
//            R.id.text_read_power_level_value,
//            rfidSettings.scan.powerLevelRead
//        )
//
//        // session
//        loadStringSetting(
//            R.id.text_session_value,
//            rfidSettings.scan.sessionFlag.toString()
//        )

        // sessionInit

        /*loadBooleanSetting(
                R.id.checkbox_session_init,
                this.rfidSettings.scan.sessionInit);*/

        // report unigue tags
//        val unigue =
//            if (true == (rfidSettings.scan.doubleReading == DoubleReading.PREVENT1)) true else false
//        loadBooleanSetting(
//            R.id.checkbox_report_unique_tags,
//            unigue
//        )

        // channel5 : 0x00000001
//        var onoff = if (rfidSettings.scan.channel and 0x00000001L > 0) true else false
//        loadBooleanSetting(
//            R.id.checkbox_channel5,
//            onoff
//        )
//
//        // channel11 : 0x00000002
//        onoff = if (rfidSettings.scan.channel and 0x00000002L > 0) true else false
//        loadBooleanSetting(
//            R.id.checkbox_channel11,
//            onoff
//        )
//
//        // channel17 : 0x00000004
//        onoff = if (rfidSettings.scan.channel and 0x00000004L > 0) true else false
//        loadBooleanSetting(
//            R.id.checkbox_channel17,
//            onoff
//        )
//
//        // channel23 : 0x00000008
//        onoff = if (rfidSettings.scan.channel and 0x00000008L > 0) true else false
//        loadBooleanSetting(
//            R.id.checkbox_channel23,
//            onoff
//        )
//
//        // channel24 : 0x00000010
//        onoff = if (rfidSettings.scan.channel and 0x00000010L > 0) true else false
//        loadBooleanSetting(
//            R.id.checkbox_channel24,
//            onoff
//        )
//
//        // channel25 : 0x00000020
//        onoff = if (rfidSettings.scan.channel and 0x00000020L > 0) true else false
//        loadBooleanSetting(
//            R.id.checkbox_channel25,
//            onoff
//        )
//
//        // q_factor
//        loadIntegerSetting(
//            R.id.text_q_factor_value,
//            rfidSettings.scan.qParam.toInt()
//        )
//
//        // link_profile
//        loadStringSetting(
//            R.id.text_link_profile_value, rfidSettings.scan.linkProfile.toString()
//        )

        // polarization
//        var strPolarization = ""
//        if (true == polarizationMap.containsValue(rfidSettings?.scan?.polarization)) {
//            // Declare Iterator <Map.Entry<String, Polarization>>
//            val itr = polarizationMap.entries.iterator()
//
//            // Acquire the key and value
//            while (itr.hasNext()) {
//                // Acquire the value using next
//                val (key, value) = itr.next()
//                if (value == rfidSettings?.scan?.polarization) {
//                    strPolarization = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(
//            R.id.text_polarization_value,
//            strPolarization
//        )
//
//
//        // power_save(RFID)
//        loadBooleanSetting(
//            R.id.checkbox_power_save,
//            rfidSettings.scan.powerSave
//        )

        // AutoLinkProfile
//        try {
//            loadBooleanSetting(
//                R.id.checkbox_auto_link_profile,
//                super.getCommScanner()!!.rfidScanner.autoLinkProfile
//            )
//        } catch (ex: RFIDException) {
//            if (ex.errorCode != ErrorCode.NOT_SUPPORT_COMMAND) {
//                Log.d("TAG", "exception when load auto link profile " + ex.errorCode)
//            }
//        }

        // reconnect
//        var reconnectValue = ""
//        if (reConnectMap.containsValue(commParams?.btbutton?.reConnect)) {
//            // Acquire the key and value
//            for ((key, value): Map.Entry<String, ReConnect> in reConnectMap) {
//                // Acquire the value using next
//                if (value == commParams.btbutton.reConnect) {
//                    reconnectValue = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(R.id.text_reconnect_value, reconnectValue)

        // Mode
//        var modeValue = ""
//        if (modeMap.containsValue(commBTSetting.mode)) {
//            // Acquire the key and value
//            for ((key, value): Map.Entry<String, CommScannerBtSettings.Mode> in modeMap) {
//                // Acquire the value using next
//                if (value == commBTSetting.mode) {
//                    modeValue = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(R.id.text_mode_value, modeValue)

        // Trying time
//        var tryingTimeValue = ""
//        if (tryingTimeMap.containsValue(commBTSetting.master.tryingTime)) {
//            // Acquire the key and value
//            for ((key, value): Map.Entry<String, TryingTime> in tryingTimeMap) {
//                // Acquire the value using next
//                if (value == commBTSetting.master.tryingTime) {
//                    tryingTimeValue = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(R.id.text_trying_time_value, tryingTimeValue)

//        // Waiting time
//        var waitingTimeValue = ""
//        if (waitingTimeMap.containsValue(commBTSetting.slave.waitingTime)) {
//            // Acquire the key and value
//            for ((key, value): Map.Entry<String, WaitingTime> in waitingTimeMap) {
//                // Acquire the value using next
//                if (value == commBTSetting.slave.waitingTime) {
//                    waitingTimeValue = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(R.id.text_waiting_time_value, waitingTimeValue)
//

//        // rfid trigger_mode
//        var strRfid_TriggerMode = ""
//        if (true == rfid_triggerModeMap.containsValue(rfidSettings.scan.triggerMode)) {
//            // Declare Iterator<Map.Entry<String, TriggerMode>>
//            val itr = rfid_triggerModeMap.entries.iterator()
//
//            // Acquire the key and value
//            while (itr.hasNext()) {
//                // Acquire the value using next
//                val (key, value) = itr.next()
//                if (value == rfidSettings.scan.triggerMode) {
//                    strRfid_TriggerMode = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(
//            R.id.text_rfid_trigger_mode_value,
//            strRfid_TriggerMode
//        )

        // [Configuration value related to the common parameters]

//        // buzzer
//        onoff = if (commParams.notification.sound.buzzer == Buzzer.ENABLE) true else false
//        loadBooleanSetting(
//            R.id.checkbox_buzzer,
//            onoff
//        )

//        // buzzer_volumes
//        var strBuzzerVolume = ""
//        if (true == buzzerVolumeMap.containsValue(commParams.buzzerVolume)) {
//            // Declare Iterator<Map.Entry<String and BuzzerVolume>>
//            val itr = buzzerVolumeMap.entries.iterator()
//
//            // Acquire the key and value
//            while (itr.hasNext()) {
//                // Acquire the value using next
//                val (key, value) = itr.next()
//                if (value == commParams.buzzerVolume) {
//                    strBuzzerVolume = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(
//            R.id.text_buzzer_volume_value,
//            strBuzzerVolume
//        )
//
//        // [Configuration value related to the barcode]
//
//        // trigger_mode
//        var strTriggerMode = ""
//        if (true == triggerModeMap.containsValue(barcodeSettings.scan.triggerMode)) {
//            // Declare Iterator<Map.Entry<String, TriggerMode>>
//            val itr = triggerModeMap.entries.iterator()
//
//            // Acquire the key and value
//            while (itr.hasNext()) {
//                // Acquire the value using next
//                val (key, value) = itr.next()
//                if (value == barcodeSettings.scan.triggerMode) {
//                    strTriggerMode = key
//                    break
//                }
//            }
//        }
//        loadStringSetting(
//            R.id.text_trigger_mode_value,
//            strTriggerMode
//        )
//
//        // enable_all_1d_codes
//        onoff = this.checkEnable1dCodes(barcodeSettings)
//        loadBooleanSetting(
//            R.id.checkbox_enable_all_1d_codes,
//            onoff
//        )
//
//        // enable_all_2d_codes
//        onoff = this.checkEnable2dCodes(barcodeSettings)
//        loadBooleanSetting(
//            R.id.checkbox_enable_all_2d_codes,
//            onoff
//        )

        */

    }

    private fun save() {
        // Stop processing in case of there was no connection with SP1 when transit to scren “Setting”.
        if (!scannerConnectedOnCreate) {
            return
        }

        // Transmit and maintain SP1 configuration value
        var failedSettings = rfidSettings == null || commParams == null || barcodeSettings == null
        var exception: java.lang.Exception? = null
        if (!failedSettings) {
            try {
                // Display “toast” to indicate “in process of setting” when connected to scanner.

                // Can not display message in UI thread due to send method is working.

                // Create another thread and display message toast there.

                // Consider the case can not write a setting even when it is connected to determine it is able to read a setting or not.
                val thread = Thread {
                    Looper.prepare()
                    showMessage(getString(R.string.I_MSG_PROGRESS_SETTING))
                    Looper.loop()
                }
                thread.start()

                // Send a setting value of RFIDScannerSettings.
                sendRFIDScannerSettings()

                // Send a setting value of CommScannerParams.
              //  sendCommScannerParams()

                // Send a setting value of BarcodeScannerSettings.
              //  sendBarcodeScannerSettings()
            } catch (e: CommException) {
                failedSettings = true
                exception = e
            } catch (e: RFIDException) {
                failedSettings = true
                exception = e
            } catch (e: BarcodeException) {
                failedSettings = true
                exception = e
            }
        }
        if (failedSettings) {
            super.showMessage(getString(R.string.E_MSG_SAVE_SETTINGS))
            exception?.printStackTrace()
            return
        }

        // Exit screen after waiting since it takes time to send and receive SP1 command.
        try {
            Thread.sleep(1500)
        } catch (e: java.lang.Exception) {
            // TODO: Catch error.
        }

        if (commParams != null) {
            super.showMessage(getString(R.string.I_MSG_SAVE_SETTINGS))
        }
        // Message “save complete”
    }

    @Throws(CommException::class, RFIDException::class)
    private fun sendRFIDScannerSettings(){
        try {
            // Set “powerLevelRead”
            rfidSettings!!.scan.powerLevelRead  =  binding.sp1PowerLevel.value.toInt()
            rfidSettings!!.scan.powerLevelWrite = binding.sp1PowerLevel.value.toInt()

            super.getCommScanner().rfidScanner.settings = rfidSettings
        }catch(e: Exception){
            // TODO: Catch error.
        }

    }


    @Throws(CommException::class)
    private fun sendCommScannerParams(){

    }

    @Throws(CommException::class, BarcodeException::class)
    private fun sendBarcodeScannerSettings(){

    }
}