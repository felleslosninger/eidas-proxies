class eidas_cidp_proxy (

  String $java_home                        = $eidas_cidp_proxy::params::java_home,
  Integer $server_port                     = $eidas_cidp_proxy::params::server_port,
  String $dsf_gateway_url                  = $eidas_cidp_proxy::params::dsf_gateway_url,
  Integer $dsf_gateway_timeout             = $eidas_cidp_proxy::params::dsf_gateway_timeout,
  Integer $dsf_gateway_retry_count         = $eidas_cidp_proxy::params::dsf_gateway_retry_count,
  String $mf_gateway_url                   = $eidas_cidp_proxy::params::mf_gateway_url,
  Integer $mf_gateway_timeout              = $eidas_cidp_proxy::params::mf_gateway_timeout,
  Integer $mf_gateway_retry_count          = $eidas_cidp_proxy::params::mf_gateway_retry_count,
  Boolean $mf_gateway_enabled              = $eidas_cidp_proxy::params::mf_gateway_enabled,
  String $eventlog_jms_url                 = $eidas_cidp_proxy::params::eventlog_jms_url,
  String $eventlog_jms_queuename           = $eidas_cidp_proxy::params::eventlog_jms_queuename,
  String $saml_proxy_idp_entity_name       = $eidas_cidp_proxy::params::saml_proxy_idp_entity_name,
  String $saml_proxy_sp_entity_name        = $eidas_cidp_proxy::params::saml_proxy_sp_entity_name,
  String $saml_idp_entity_name             = $eidas_cidp_proxy::params::saml_idp_entity_name,
  String $saml_idp_base_url                = $eidas_cidp_proxy::params::saml_idp_base_url,
  String $log_level                        = $eidas_cidp_proxy::params::log_level,
  String $auditlog_dir                     = $eidas_cidp_proxy::params::auditlog_dir,
  String $auditlog_file                    = $eidas_cidp_proxy::params::auditlog_file,
  Boolean $eidas_cidp_proxy_dummy_enabled  = $eidas_cidp_proxy::params::eidas_cidp_proxy_dummy_enabled,
  Integer $saml_instant_issue_time_to_live = $eidas_cidp_proxy::params::saml_instant_issue_time_to_live,
  Integer $saml_instant_issue_time_skew    = $eidas_cidp_proxy::params::saml_instant_issue_time_skew,
  String $saml_requester                   = $eidas_cidp_proxy::params::saml_requester,
  String $saml_responder                   = $eidas_cidp_proxy::params::saml_responder,
  Integer $eidas_cidp_proxy_timeout        = $eidas_cidp_proxy::params::eidas_cidp_proxy_timeout,
  Integer $eidas_cidp_proxy_retry_count    = $eidas_cidp_proxy::params::eidas_cidp_proxy_retry_count,
  String $cidpproxy_keystore_password      = $eidas_cidp_proxy::params::cidpproxy_keystore_password,
  String $cidpproxy_privatekey_password    = $eidas_cidp_proxy::params::cidpproxy_privatekey_password,
  String $cidpproxy_keystore_location      = $eidas_cidp_proxy::params::cidpproxy_keystore_location,
  String $cidpproxy_keystore_alias         = $eidas_cidp_proxy::params::cidpproxy_keystore_alias,
  String $cidpproxy_keystore_type          = $eidas_cidp_proxy::params::cidpproxy_keystore_type,
  String $cidpproxy_keystore_serial_number = $eidas_cidp_proxy::params::cidpproxy_keystore_serial_number,
  String $cidpproxy_keystore_issuer        = $eidas_cidp_proxy::params::cidpproxy_keystore_issuer,
  Hash $key_store                          = $eidas_cidp_proxy::params::key_store,
  Hash $trust_store                        = $eidas_cidp_proxy::params::trust_store,
  String $security_providers               = $eidas_cidp_proxy::params::security_providers,
  String $install_dir                      = $eidas_cidp_proxy::params::install_dir,
  String $config_dir                       = $eidas_cidp_proxy::params::config_dir,
  String $log_root                         = $eidas_cidp_proxy::params::log_root,
  String $application                      = $eidas_cidp_proxy::params::application,
  String $service_name                     = $eidas_cidp_proxy::params::service_name,
  String $context                          = $eidas_cidp_proxy::params::context,
  String $artifact_id                      = $eidas_cidp_proxy::params::artifact_id,
  String $sp_url                           = $eidas_cidp_proxy::params::sp_url,
  String $cidpproxy_url                    = $eidas_cidp_proxy::params::cidpproxy_url,
  Boolean $oidc_enable                     = $eidas_cidp_proxy::params::oidc_enable,
  String $oidc_issuer_uri                  = $eidas_cidp_proxy::params::oidc_issuer_uri,
  String $oidc_client_id                   = $eidas_cidp_proxy::params::oidc_client_id,
  String $oidc_client_secret               = $eidas_cidp_proxy::params::oidc_client_secret,
  String $oidc_redirect_uri                = $eidas_cidp_proxy::params::oidc_redirect_uri,
  String $health_show_details              = $eidas_cidp_proxy::params::health_show_details,
  String $tomcat_tmp_dir                   = $eidas_cidp_proxy::params::tomcat_tmp_dir,
  Integer $server_tomcat_max_threads       = $eidas_cidp_proxy::params::server_tomcat_max_threads,
  Integer $server_tomcat_min_spare_threads = $eidas_cidp_proxy::params::server_tomcat_min_spare_threads,


) inherits eidas_cidp_proxy::params {

  include platform

  anchor { 'eidas_cidp_proxy::begin': } ->
  class { '::eidas_cidp_proxy::install': } ->
  class { '::eidas_cidp_proxy::deploy': } ->
  class { '::eidas_cidp_proxy::test_setup': } ->
  class { '::eidas_cidp_proxy::config': } ~>
  class { '::eidas_cidp_proxy::service': } ->
  anchor { 'eidas_cidp_proxy::end': }

}
