package nl.jovmit.loctrack.sdk.submit

class SystemClock : Clock {

    override fun now(): Long = System.currentTimeMillis()
}