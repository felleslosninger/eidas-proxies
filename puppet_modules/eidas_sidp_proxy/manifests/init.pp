class eidas_sidp_proxy (

  String $java_home                               = $eidas_sidp_proxy::params::java_home,
  String $eidas_sidp_proxy_url                    = $eidas_sidp_proxy::params::eidas_sidp_proxy_url,
  String $eventlog_jms_url                        = $eidas_sidp_proxy::params::eventlog_jms_url,
  String $eventlog_jms_queuename                  = $eidas_sidp_proxy::params::eventlog_jms_queuename,
  String $saml_proxy_idp_entity_name              = $eidas_sidp_proxy::params::saml_proxy_idp_entity_name,
  String $eidas_node_url                          = $eidas_sidp_proxy::params::eidas_node_url,
  String $proxy_auth_url                          = $eidas_sidp_proxy::params::proxy_auth_url,
  String $log_level                               = $eidas_sidp_proxy::params::log_level,
  Hash $available_countries                       = $eidas_sidp_proxy::params::available_countries,
  String $iso_country_mapper_file                 = $eidas_sidp_proxy::params::iso_country_mapper_file,
  String $auditlog_dir                            = $eidas_sidp_proxy::params::auditlog_dir,
  String $auditlog_file                           = $eidas_sidp_proxy::params::auditlog_file,
  Integer $fileconfig_read_period                  = $eidas_sidp_proxy::params::fileconfig_read_period,
  String $fileconfig_countries_attributes         = $eidas_sidp_proxy::params::fileconfig_countries_attributes,
  Boolean $eidas_sidp_proxy_dummy_enabled         = $eidas_sidp_proxy::params::eidas_sidp_proxy_dummy_enabled,
  Integer $saml_instant_issue_time_to_live        = $eidas_sidp_proxy::params::saml_instant_issue_time_to_live,
  Integer $saml_instant_issue_time_skew           = $eidas_sidp_proxy::params::saml_instant_issue_time_skew,
  String $saml_requester                          = $eidas_sidp_proxy::params::saml_requester,
  String $saml_responder                          = $eidas_sidp_proxy::params::saml_responder,
  Boolean $saml_check_certificate_validity_period = $eidas_sidp_proxy::params::saml_check_certificate_validity_period,
  Boolean $saml_disallow_self_signed_certificate  = $eidas_sidp_proxy::params::saml_disallow_self_signed_certificate,
  Boolean $saml_ip_address_validation             = $eidas_sidp_proxy::params::saml_ip_address_validation,
  String $idporten_keystore_password              = $eidas_sidp_proxy::params::idporten_keystore_password,
  String $idporten_keystore_privatekey_password   = $eidas_sidp_proxy::params::idporten_keystore_privatekey_password,
  String $idporten_keystore_location              = $eidas_sidp_proxy::params::idporten_keystore_location,
  String $idporten_keystore_alias                 = $eidas_sidp_proxy::params::idporten_keystore_alias,
  String $idporten_keystore_type                  = $eidas_sidp_proxy::params::idporten_keystore_type,
  Tuple $mf_test_users                            = $eidas_sidp_proxy::params::mf_test_users,
  String $mf_gateway_url                          = $eidas_sidp_proxy::params::mf_gateway_url,
  String $mf_gateway_username                     = $eidas_sidp_proxy::params::mf_gateway_username,
  String $mf_gateway_password                     = $eidas_sidp_proxy::params::mf_gateway_password,
  Integer $mf_gateway_timeout                     = $eidas_sidp_proxy::params::mf_gateway_timeout,
  Integer $mf_gateway_retry_count                 = $eidas_sidp_proxy::params::mf_gateway_retry_count,
  Hash $key_store                                 = $eidas_sidp_proxy::params::key_store,
  Hash $trust_store                               = $eidas_sidp_proxy::params::trust_store,
  String $security_providers                      = $eidas_sidp_proxy::params::security_providers,
  Integer $server_port                            = $eidas_sidp_proxy::params::server_port,
  String $config_dir                              = $eidas_sidp_proxy::params::config_dir,
  String $log_root                                = $eidas_sidp_proxy::params::log_root,
  String $application                             = $eidas_sidp_proxy::params::application,
  String $context                                 = $eidas_sidp_proxy::params::context,
  String $artifact_id                             = $eidas_sidp_proxy::params::artifact_id,
  String $health_show_details                     = $eidas_sidp_proxy::params::health_show_details,
  String $tomcat_tmp_dir                          = $eidas_sidp_proxy::params::tomcat_tmp_dir,
  Integer $server_tomcat_max_threads              = $eidas_sidp_proxy::params::server_tomcat_max_threads,
  Integer $server_tomcat_min_spare_threads        = $eidas_sidp_proxy::params::server_tomcat_min_spare_threads,

)inherits eidas_sidp_proxy::params {

  include platform

  anchor { 'eidas_sidp_proxy::begin': } ->
  class { '::eidas_sidp_proxy::install': } ->
  class { '::eidas_sidp_proxy::deploy': } ->
  class { '::eidas_sidp_proxy::test_setup': } ->
  class { '::eidas_sidp_proxy::config': } ~>
  class { '::eidas_sidp_proxy::service': } ->
  anchor { 'eidas_sidp_proxy::end': }

}
