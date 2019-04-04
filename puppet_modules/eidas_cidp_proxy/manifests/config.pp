class eidas_cidp_proxy::config inherits eidas_cidp_proxy{

  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/eidas-cidp-proxy.properties":
    ensure  => 'file',
    content => template("${module_name}/eidas-cidp-proxy.properties.erb"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  } ->
  difilib::logback_config { $eidas_cidp_proxy::application:
    application       => $eidas_cidp_proxy::application,
    owner             => $eidas_cidp_proxy::service_name,
    group             => $eidas_cidp_proxy::service_name,
    resilience        => false,
    performance_class => '',
    loglevel_no       => $eidas_cidp_proxy::log_level,
    loglevel_nondifi  => $eidas_cidp_proxy::log_level,
  } ->
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/messages/eidas-cidp-proxy-messages_en.properties":
    ensure  => 'file',
    content => template("${module_name}/messages/eidas-cidp-proxy-messages_en.properties"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/messages/eidas-cidp-proxy-messages_nn.properties":
    ensure  => 'file',
    content => template("${module_name}/messages/eidas-cidp-proxy-messages_nn.properties"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/messages/eidas-cidp-proxy-messages_nb.properties":
    ensure  => 'file',
    content => template("${module_name}/messages/eidas-cidp-proxy-messages_nb.properties"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/messages/eidas-cidp-proxy-messages_se.properties":
    ensure  => 'file',
    content => template("${module_name}/messages/eidas-cidp-proxy-messages_se.properties"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/application.yml":
    ensure  => 'file',
    content => template("${module_name}/application.yml.erb"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/SignModule.properties":
    ensure  => 'file',
    content => template("${module_name}/SignModule.properties.erb"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::install_dir}${eidas_cidp_proxy::application}/${eidas_cidp_proxy::application}.conf":
    ensure  => 'file',
    content => template("${module_name}/eidas-cidp-proxy.conf.erb"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/saml-engine-eidas-attributes.xml":
    ensure => 'file',
    source => "puppet:///modules/${module_name}/saml-engine-eidas-attributes.xml",
    group  => $eidas_cidp_proxy::service_name,
    owner  => $eidas_cidp_proxy::service_name,
    mode   => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/saml-engine-additional-attributes.xml":
    ensure  => 'file',
    content => template('eidas_additional_attribute/saml-engine-additional-attributes.xml.erb'),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/SamlEngine.xml":
      ensure  => 'file',
      content => template("${module_name}/SamlEngine.xml.erb"),
      group   => $eidas_cidp_proxy::service_name,
      owner   => $eidas_cidp_proxy::service_name,
      mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/SamlEngine_eidas-cidp-proxy.xml":
    ensure  => 'file',
    content => template("${module_name}/SamlEngine_eidas-cidp-proxy.xml.erb"),
    group   => $eidas_cidp_proxy::service_name,
    owner   => $eidas_cidp_proxy::service_name,
    mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/EncryptModule.xml":
      ensure  => 'file',
      content => template("${module_name}/EncryptModule.xml.erb"),
      group   => $eidas_cidp_proxy::service_name,
      owner   => $eidas_cidp_proxy::service_name,
      mode    => '0644',
  }
  file { "${eidas_cidp_proxy::config_dir}${eidas_cidp_proxy::application}/encryptionConf.xml":
      ensure  => 'file',
      content => template("${module_name}/encryptionConf.xml.erb"),
      group   => $eidas_cidp_proxy::service_name,
      owner   => $eidas_cidp_proxy::service_name,
      mode    => '0644',
  }
  }
