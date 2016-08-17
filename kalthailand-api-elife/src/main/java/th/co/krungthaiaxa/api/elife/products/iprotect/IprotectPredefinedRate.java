package th.co.krungthaiaxa.api.elife.products.iprotect;

public class IProtectPredefinedRate {
    private final IProtectPackage iprotectPackage;
    private final int age;
    private final double maleRate;
    private final double femaleRate;

    public IProtectPredefinedRate(IProtectPackage iprotectPackage, int age, double maleRate, double femaleRate) {
        this.iprotectPackage = iprotectPackage;
        this.age = age;
        this.maleRate = maleRate;
        this.femaleRate = femaleRate;
    }

    public int getAge() {
        return age;
    }

    public IProtectPackage getIprotectPackage() {
        return iprotectPackage;
    }

    public double getMaleRate() {
        return maleRate;
    }

    public double getFemaleRate() {
        return femaleRate;
    }

}
