package org.jpromise;

public record SettledResult<T>(Status status, T value, Exception reason) {
    public enum Status {
        FULFILLED, REJECTED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static <T> SettledResult<T> fulfilled(T value) {
        return new SettledResult<>(Status.FULFILLED, value, null);
    }

    public static <T> SettledResult<T> rejected(Exception reason) {
        return new SettledResult<>(Status.REJECTED, null, reason);
    }
}
