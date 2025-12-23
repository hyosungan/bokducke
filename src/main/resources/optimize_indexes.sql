-- housedeals 테이블 성능 최적화를 위한 인덱스 추가
-- apt_seq에 인덱스가 없으면 JOIN 성능이 저하될 수 있습니다

-- apt_seq 인덱스 추가 (이미 FOREIGN KEY가 있다면 인덱스가 있을 수 있음)
-- 먼저 인덱스가 있는지 확인 후 없으면 추가
CREATE INDEX IF NOT EXISTS idx_housedeals_apt_seq ON housedeals(apt_seq);

-- no와 apt_seq 복합 인덱스 (최신 거래 조회 최적화)
CREATE INDEX IF NOT EXISTS idx_housedeals_apt_seq_no ON housedeals(apt_seq, no DESC);

-- houseinfos 테이블의 umd_nm 인덱스 (필터링 최적화)
CREATE INDEX IF NOT EXISTS idx_houseinfos_umd_nm ON houseinfos(umd_nm);

