package fj.ac.usp.spacehub.model;
public enum BookingType { LAB, TUTORIAL, ECA;
    public boolean isAcademic(){ return this == LAB || this == TUTORIAL; }
}
