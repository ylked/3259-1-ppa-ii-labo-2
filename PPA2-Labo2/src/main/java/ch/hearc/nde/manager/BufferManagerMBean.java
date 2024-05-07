package ch.hearc.nde.manager;

public interface BufferManagerMBean {

    int getNbProducers();
    int getNbConsumers();
    int getBufferSize();

    int getCurrentBufferSize();

    void setNbProducers(int n);
    void setNbConsumers(int n);
    void setBufferSize(int n);
    double getAverageLatency();
    double getAverageDebit();
    int getSampleSize();
    void setSampleSize(int n);
}
