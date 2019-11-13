-keepparameternames
-keep class tech.yaog.hardwares.serialport.*{
    public *;
    public <methods>;
    protected <methods>;
}
-keepclassmembernames class tech.yaog.hardwares.serialport.SerialPort {
    private java.io.FileDescriptor mFd;
}