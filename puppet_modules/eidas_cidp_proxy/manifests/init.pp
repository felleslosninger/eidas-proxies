class eidas_cidp_proxy (

  $dsf_gateway_url                        = $eidas_cidp_proxy::params::dsf_gateway_url,
  $mf_gateway_url                         = $eidas_cidp_proxy::params::mf_gateway_url,
  $eventlog_jms_url                       = $eidas_cidp_proxy::params::eventlog_jms_url,
  $eventlog_jms_queuename                 = $eidas_cidp_proxy::params::eventlog_jms_queuename,
  $saml_proxy_idp_entity_name             = $eidas_cidp_proxy::params::saml_proxy_idp_entity_name,
  $saml_proxy_sp_entity_name              = $eidas_cidp_proxy::params::saml_proxy_sp_entity_name,
  $saml_idp_entity_name                   = $eidas_cidp_proxy::params::saml_idp_entity_name,
  $saml_idp_base_url                      = $eidas_cidp_proxy::params::saml_idp_base_url,
  $log_level                              = $eidas_cidp_proxy::params::log_level,
  $auditlog_dir                           = $eidas_cidp_proxy::params::auditlog_dir,
  $auditlog_file                          = $eidas_cidp_proxy::params::auditlog_file,
  Integer $server_port                    = $eidas_cidp_proxy::params::server_port,
  $eidas_cidp_proxy_dummy_enabled         = $eidas_cidp_proxy::params::eidas_cidp_proxy_dummy_enabled,
  $saml_instant_issue_time_to_live        = $eidas_cidp_proxy::params::saml_instant_issue_time_to_live,
  $saml_instant_issue_time_skew           = $eidas_cidp_proxy::params::saml_instant_issue_time_skew,
  $saml_requester                         = $eidas_cidp_proxy::params::saml_requester,
  $saml_responder                         = $eidas_cidp_proxy::params::saml_responder,
  $eidas_cidp_proxy_timeout               = $eidas_cidp_proxy::params::eidas_cidp_proxy_timeout,
  $eidas_cidp_proxy_retry_count           = $eidas_cidp_proxy::params::eidas_cidp_proxy_retry_count,
  $cidpproxy_keystore_password            = $eidas_cidp_proxy::params::cidpproxy_keystore_password,
  $cidpproxy_privatekey_password          = $eidas_cidp_proxy::params::cidpproxy_privatekey_password,
  $cidpproxy_keystore_location            = $eidas_cidp_proxy::params::cidpproxy_keystore_location,
  $cidpproxy_keystore_alias               = $eidas_cidp_proxy::params::cidpproxy_keystore_alias,
  $cidpproxy_keystore_type                = $eidas_cidp_proxy::params::cidpproxy_keystore_type,
  $key_store                              = $eidas_cidp_proxy::params::key_store,
  $trust_store                            = $eidas_cidp_proxy::params::trust_store,
  $security_providers                     = $eidas_cidp_proxy::params::security_providers,
  $config_root                            = $eidas_cidp_proxy::params::config_root,
  $log_root                               = $eidas_cidp_proxy::params::log_root,
  $application                            = $eidas_cidp_proxy::params::application,
  $context                                = $eidas_cidp_proxy::params::context,
  $oidc_enable                            = $eidas_cidp_proxy::params::oidc_enable,
  $oidc_issuer_uri                        = $eidas_cidp_proxy::params::oidc_issuer_uri,
  $oidc_client_id                         = $eidas_cidp_proxy::params::oidc_client_id,
  $oidc_client_secret                     = $eidas_cidp_proxy::params::oidc_client_secret,
  $oidc_redirect_uri                      = $eidas_cidp_proxy::params::oidc_redirect_uri,


)inherits eidas_cidp_proxy::params {

  include platform

  anchor { 'eidas_cidp_proxy::begin': } ->
  class { '::eidas_cidp_proxy::install': } ->
  class { '::eidas_cidp_proxy::deploy': } ->
  class { '::eidas_cidp_proxy::test_setup': } ->
  class { '::eidas_cidp_proxy::config': } ~>
  class { '::eidas_cidp_proxy::service': } ->
  anchor { 'eidas_cidp_proxy::end': }

}
