INSERT INTO `metrics` (metric_code, metric_name_en, metric_name_ko, unit)
VALUES
  ('ROA', 'ROA', 'ROA (총자산이익률)', '%'),
  ('ROE', 'ROE', 'ROE (자기자본이익률)', '%'),
  ('OpMargin', 'OperatingProfitMargin', '매출액영업이익률', '%'),
  ('DbRatio', 'DebtToEquityRatio', '부채비율', '%'),
  ('EqRatio', 'EquityRatio', '자기자본비율', '%'),
  ('CapImpRatio', 'CapitalImpairmentRatio', '자본잠식률', '%'),
  ('STDebtRatio', 'ShortTermDebtRatio', '단기차입금비율', '%'),
  ('CurRatio', 'CurrentRatio', '유동비율', '%'),
  ('QkRatio', 'QuickRatio', '당좌비율', '%'),
  ('CurLibRatio', 'CurrentLiabilitiesRatio', '유동부채비율', '%'),
  ('CFO_AsRatio', 'CashFlowFromOperationsToAssetsRatio', 'CFO_자산비율', '%'),
  ('CFO_Sale', 'CashFlowFromOperationsToRevenueRatio', 'CFO_매출액비율', '%'),
  ('CFO_GR', 'CashFlowFromOperationsGrowthRate', 'CFO증감률', '%')
ON DUPLICATE KEY UPDATE
  metric_name_en = VALUES(metric_name_en),
  metric_name_ko = VALUES(metric_name_ko),
  unit = VALUES(unit);
