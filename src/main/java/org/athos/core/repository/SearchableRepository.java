package org.athos.core.repository;

import io.github.perplexhub.rsql.RSQLJPASupport;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SearchableRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

  default Page<T> findAll(String query, Pageable pageable) {
    return StringUtils.isNotBlank(query)
        ? findAll(RSQLJPASupport.toSpecification(query), pageable)
        : findAll(pageable);
  }

}
