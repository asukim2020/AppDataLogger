package kr.co.greentech.dataloggerapp.fragment.channel.enums

enum class FilterType(val value: Int) {
    HZ10(0),
    HZ100(1),
    KHZ1(2),
    KHZ10(3);

    fun getTitle(): String {
        return when(this) {
            HZ10 -> "10Hz"
            HZ100 -> "100Hz"
            KHZ1 -> "1kHz"
            KHZ10 -> "10kHz"
        }
    }
}