package com.volodymyrkozlov.tradingdatamanager.dto;

import static com.volodymyrkozlov.tradingdatamanager.utils.ValidationUtils.validateRequired;

public record FinancialDataResponse(Double min,
                                    Double max,
                                    Double last,
                                    Double avg,
                                    Double var) {

    public FinancialDataResponse {
        validateRequired(min, "min");
        validateRequired(max, "max");
        validateRequired(last, "last");
        validateRequired(avg, "avg");
        validateRequired(var, "var");
    }

    public static Builder financialDataResponseBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Double min;
        private Double max;
        private Double last;
        private Double avg;
        private Double var;

        public Builder min(Double min) {
            this.min = min;
            return this;
        }

        public Builder max(Double max) {
            this.max = max;
            return this;
        }

        public Builder last(Double last) {
            this.last = last;
            return this;
        }

        public Builder avg(Double avg) {
            this.avg = avg;
            return this;
        }

        public Builder var(Double var) {
            this.var = var;
            return this;
        }

        public FinancialDataResponse build() {
            return new FinancialDataResponse(min, max, last, avg, var);
        }
    }

}
